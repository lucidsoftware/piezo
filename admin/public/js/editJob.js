(function () {
    var renumberDataMap = function() {
        Array.prototype.reduce.call($('.job-data-map .form-group'), function (i, next) {
            var label = $(next).children('label');
            label.attr('for', label.attr('for').replace(/_\d+__/, '_' + i + '__'));

            var input = $(next).find('input');
            input.attr('id', input.attr('id').replace(/_\d+__/, '_' + i + '__'));
            input.attr('name', input.attr('name').replace(/\[\d+\]/, '[' + i + ']'));

            if (input.attr('id').match(/job-data-map_\d+__value/)) {
                return i + 1;
            }

            return i;
        }, 0);
    }
    
    $('.job-data-map').on('click', '.job-data-delete a', function () {
        $(this).parent().next().next().remove();
        $(this).parent().next().remove();
        $(this).parent().remove();

        renumberDataMap();
    });

    $('.job-data-add').click(function () {
        var key = $(this).prev().prev().clone();
        var value = $(this).prev().clone();

        $(this).prev().prev().before($('<div class="job-data-delete text-right"><a href="#">delete</a></div>'));

        key.find('input').val('');
        value.find('input').val('');
        
        $(this).before(key);
        $(this).before(value);

        renumberDataMap();
    });

    $('form').submit(function () {
        $('.job-data-map .form-group').each(function () {
            var keyInput = $(this).find('input');
            if (keyInput.attr('name').match(/.*key$/)) {
                var valueInput = $(this).next().find('input');

                if (keyInput.val() === '' && valueInput.val() === '') {
                    $(this).next().remove();
                    $(this).remove();
                    renumberDataMap();
                }
            }
        });
    });
})();