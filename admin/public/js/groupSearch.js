(function () {
    document.querySelectorAll('[data-group-search]').forEach((root) => {
        const search = root.querySelector('.group-search-input');
        const list = root.querySelector('.group-search-list');
        const emptyMessage = root.querySelector('.group-search-empty');

        if (!search || !list || !emptyMessage) {
            return;
        }

        let searchActive = false;

        const groups = Array.from(list.children).filter(
            (element) => element.classList.contains('accordion-item')
        ).map((panel) => {
            const collapse = Array.from(panel.children).find(
                (element) => element.classList.contains('accordion-collapse')
            );
            const header = panel.querySelector('.group-search-group-name');
            const itemList = panel.querySelector('.list-group');

            if (!collapse || !header || !itemList) {
                return null;
            }

            return {
                panel: panel,
                collapse: collapse,
                header: header,
                itemList: itemList,
                items: Array.from(itemList.querySelectorAll('.group-search-item')),
                parent: collapse.getAttribute('data-bs-parent'),
                expandedBeforeSearch: false,
                hasActiveItem: !!itemList.querySelector('.group-search-item.active')
            };
        }).filter((group) => group !== null);

        const storageKey = 'piezo-group-search';
        try {
            search.value = window.sessionStorage.getItem(storageKey) || '';
        } catch (error) {
            // Searching should still work when browser storage is unavailable.
        }

        function storeQuery() {
            try {
                if (search.value) {
                    window.sessionStorage.setItem(storageKey, search.value);
                } else {
                    window.sessionStorage.removeItem(storageKey);
                }
            } catch (error) {
                // Searching should still work when browser storage is unavailable.
            }
        }

        function setExpanded(group, expanded) {
            // Keep Bootstrap's collapse element and its button synchronized.
            group.collapse.classList.toggle('show', expanded);
            group.header.classList.toggle('collapsed', !expanded);
            group.header.setAttribute('aria-expanded', String(expanded));
        }

        function setSearchMode(active) {
            root.classList.toggle('group-search-active', active);

            groups.forEach((group) => {
                const bootstrapCollapse = window.bootstrap && window.bootstrap.Collapse;
                const instance = bootstrapCollapse && bootstrapCollapse.getInstance(group.collapse);

                // Collapse instances cache their parent when they are created.
                // Recreate them on the next click with the new mode's setting.
                if (instance) {
                    instance.dispose();
                }

                if (active) {
                    // Without a shared parent, Bootstrap animates each search
                    // result independently instead of closing its siblings.
                    group.collapse.removeAttribute('data-bs-parent');
                } else if (group.parent !== null) {
                    group.collapse.setAttribute('data-bs-parent', group.parent);
                }
            });
        }

        function filterItems() {
            const query = search.value.trim().toLowerCase();
            const matches = [];
            const wasSearchActive = searchActive;
            searchActive = !!query;

            if (searchActive && !wasSearchActive) {
                groups.forEach((group) => {
                    group.expandedBeforeSearch = group.collapse.classList.contains('show');
                });
                setSearchMode(true);
            } else if (!searchActive && wasSearchActive) {
                setSearchMode(false);
            }

            groups.forEach((group) => {
                const matchingItems = [];
                const groupOnlyItems = [];

                const groupNameMatches = group.header.textContent.toLowerCase().includes(query);

                group.items.forEach((item) => {
                    const itemMatches = item.textContent.toLowerCase().includes(query);

                    if (itemMatches) {
                        matchingItems.push(item);
                    } else if (groupNameMatches) {
                        groupOnlyItems.push(item);
                    }

                    item.hidden = searchActive && !groupNameMatches && !itemMatches;
                    item.classList.toggle(
                        'group-search-group-only',
                        searchActive && groupNameMatches && !itemMatches
                    );
                });

                if (searchActive && groupNameMatches && matchingItems.length > 0) {
                    // If we match both the group name and some of the inner item names, we should re-add the items
                    //  so the matched items are put first in the list.
                    matchingItems.concat(groupOnlyItems).forEach((item) => {
                        group.itemList.appendChild(item);
                    });
                } else {
                    // Re-add all the items to the group in their original order in case we previously reordered it.
                    group.items.forEach((item) => {
                        group.itemList.appendChild(item);
                    });
                }

                if (!searchActive || groupNameMatches || matchingItems.length > 0) {
                    matches.push({
                        group: group,
                        groupNameMatches: groupNameMatches,
                        matchingItems: matchingItems.length
                    });
                }
            });

            groups.forEach((group) => {
                group.panel.hidden = true;
            });

            matches.forEach((match) => {
                match.group.panel.hidden = false;

                if (searchActive) {
                    setExpanded(
                        match.group,
                        match.matchingItems > 0 || match.group.hasActiveItem || matches.length === 1
                    );
                } else if (wasSearchActive) {
                    setExpanded(match.group, match.group.expandedBeforeSearch);
                }
            });

            emptyMessage.hidden = !searchActive || matches.length > 0;
        }

        search.addEventListener('input', () => {
            storeQuery();
            filterItems();
        });
        search.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                search.value = '';
                storeQuery();
                filterItems();
            }
        });

        filterItems();
    });
})();
