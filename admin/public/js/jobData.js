(function () {
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

        fixDataMapIndexes();
    });

    $('.job-data-add').click(function () {
        var key = $(this).prev().prev().clone();
        var value = $(this).prev().clone();

        $(this).prev().prev().before($('<div class="job-data-delete text-right"><a href="#">delete</a></div>'));

        key.find('input').val('');
        value.find('input').val('');

        $(this).before(key);
        $(this).before(value);

        fixDataMapIndexes();
    });

    $('form').submit(function () {
        fixDataMapIndexes();
    });
})();