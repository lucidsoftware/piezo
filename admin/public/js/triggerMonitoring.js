window.addEventListener('load', () => {
    const priorityInput = document.getElementById('triggerMonitoringPriority');
    const setMonitoringFieldVisibility = () => {
        const priority = priorityInput.value;
        const monitoringDetails = document.getElementById('triggerMonitoringDetails');
        if (priority == 'Off') {
            monitoringDetails.style.display = 'none'; // hide
        } else {
            monitoringDetails.style.display = 'block'; // show
        }
    };

    priorityInput.addEventListener('change', setMonitoringFieldVisibility);
    setMonitoringFieldVisibility();
}, {once: true});
