(function () {
	var baseUrl = '/typeahead/';
	var jobUrl = baseUrl + 'jobs/';
	var triggerUrl = baseUrl + 'triggers/';

	function sourceFunc(url, key, groupInput) {
		var appender = groupInput && groupInput.val()?
				function() { return groupInput.val() + '/'; } :
				function() { return ''; };
		
		return function(request, response) {
			var append = appender();

			if (!append) {
				response([]);
			} else {
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