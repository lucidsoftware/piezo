(function () {
	var baseUrl = '/typeahead/';
	var jobUrl = baseUrl + 'jobs/';
	var triggerUrl = baseUrl + 'triggers/';

	function sourceFunc(url, key, groupInput) {
		return function(request, response) {
			if (groupInput && !groupInput.val()) {
				response([]);
			} else {
				var append = (groupInput && groupInput.val()) ?
						groupInput.val() + '/' : '';
				
				$.get(url + append + request.term, function(data) {
					response(data[key]);
				});
			}
		};
	}

	$('input.job-group-type-ahead').autocomplete({
		source: sourceFunc(jobUrl, 'groups'),
	});

	$('input.job-name-type-ahead').autocomplete({
		source: sourceFunc(jobUrl, 'jobs', $('#jobGroup')),
	});

	$('input.trigger-group-type-ahead').autocomplete({
		source: sourceFunc(triggerUrl, 'groups'),
	});
})();