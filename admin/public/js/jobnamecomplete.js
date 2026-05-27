{
    let currentJobGroup = '';

    const updateJobNames= async function() {
        const jobGroup = this.value;

        const datalist = document.getElementById('job-names-list');

        if (jobGroup == currentJobGroup) {
            return;
        }

        // Empty the node
        datalist.textContent = '';

        if (!jobGroup) {
            // If we have an empty grop don't bother fetching the names
            return;
        }

        // TODO: should we cache this?
        const resp = await fetch(`/typeahead/jobs/${jobGroup}`);
        const results = await resp.json();
        for (const name of results) {
            const opt = document.createElement('option');
            opt.value = name;
            datalist.appendChild(opt);
        }
        currentJobGroup = jobGroup;
    }

    const jobGroupInput = document.getElementById('jobGroup');
    jobGroupInput?.addEventListener('change', updateJobNames);
    // Call in case we already have a value
    jobGroupInput && updateJobNames.call(jobGroupInput);
}
