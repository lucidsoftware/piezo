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
    };

    var fixDataMapIndexes = function() {
        $('[id*=job-data-map]').each(function(i, element){
            if (i % 2 === 0) {
                var itemNumber = i / 2;
                element.id = 'job-data-map_' + itemNumber + '_key';
                element.name = 'job-data-map[' + itemNumber + '].key';
            }

            if (i % 2 !== 0) {
                var itemNumber = (i - 1) / 2;
                element.id = 'job-data-map_' + itemNumber + '_value';
                element.name = 'job-data-map[' + itemNumber + '].value';
            }
        })
    };

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
        fixDataMapIndexes();
    });
})();