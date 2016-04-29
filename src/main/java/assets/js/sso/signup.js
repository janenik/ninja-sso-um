(function() {
    /** Sets up a connection between bootstrap dropdown and hidden field. */
    var setUpDropdown = function(fieldId, opt_callback) {
        $('#' + fieldId + 'Dropdown li').on('click touchend', function(e) {
            e.preventDefault();
            var li = $(e.target).closest('li');
            var attrValue = li.attr('rel');
            var title = opt_callback ? opt_callback(li) : li.find('a').html();
            $('#' + fieldId).val(attrValue);
            $('#' + fieldId + 'Title').html(title);
         });
        var preloadValue = $('#' + fieldId).val();
        if (preloadValue != '') {
            var li = $('#' + fieldId + 'Dropdown li[rel=' + preloadValue + ']');
            var fieldTitle = opt_callback ? opt_callback(li) : li.find('a').html();
            $('#' + fieldId + 'Title').html(fieldTitle);
        }
    };

    /** Birthday month, gender, country. */
    setUpDropdown('birthMonth');
    setUpDropdown('gender');
    setUpDropdown('countryId', function(li) {
        var phone = $('#phone');
        if (phone.val().length < 5) {
            $('#phone').val('+' + li.attr('rel2') + ' ');
            $('#phone')[0].focus();
        }
        return li.attr('rel3');
    });
 })();
