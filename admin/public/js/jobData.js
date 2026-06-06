(function () {
    function fixDataMapIndexes() {
        const inputs = document.querySelectorAll('input[id*=job-data-map_]');
        for (let i = 0; i < inputs.length; i++) {
            const element = inputs[i];
            if (i % 2 === 0) {
                const itemNumber = i / 2;
                element.id = 'job-data-map_' + itemNumber + '_key';
                element.name = 'job-data-map[' + itemNumber + '].key';
            } else {
                const itemNumber = (i - 1) / 2;
                element.id = 'job-data-map_' + itemNumber + '_value';
                element.name = 'job-data-map[' + itemNumber + '].value';
            }
        }
    }

    document.getElementById('job-data-map')?.addEventListener('click', function(e) {
        // Only act on clicks on the delete link
        const anchor = e.target.closest('.job-data-delete');
        if (anchor) {
            e.preventDefault();
            const entry = anchor.closest('.job-data-entry');
            entry?.remove();
            fixDataMapIndexes();
        }
    });

    document.getElementById('job-data-add')?.addEventListener('click', function() {
        const parent = this.parentElement;
        const prevEntry = parent.previousElementSibling;

        // clone before we add the delete button, or we end up with duplicate delete buttons
        const entry = prevEntry.cloneNode(true);

        prevEntry.querySelector('.job-data-delete').classList.add('shown');

        // reset inputs
        const inputs = entry.getElementsByTagName('input');
        for (const input of inputs) {
            input.value = '';
        }

        parent.before(entry);

        fixDataMapIndexes();
    });

    document.querySelector('form')?.addEventListener('submit', function() {
        fixDataMapIndexes();
    });
})();
