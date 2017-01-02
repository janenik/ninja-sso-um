var sso = sso || {};
sso.htmlHelpers = sso.htmlHelpers || {};

/**
 * <#--
 * Sets up a connection between Bootstrap dropdown and hidden field in form. If fieldId is a select tag then
 * correctly sets up behavior for callback.
 * @param {!string} fieldId Id of the field in the form. Dropdown is expected to have id equal to
 *    field id plus 'Dropdown' postfix.
 * @param {?Function} opt_callback Optional callback that searches a title in given list item element (li).
 * -->
 */
sso.htmlHelpers.setUpDropdown = function(fieldId, opt_callback) {
    $('#' + fieldId + 'Dropdown li').on('click touchend', function(e) {
        e.preventDefault();
        /**<#-- Hides Bootstrap dropdown for touchscreen devices, not done as with desktop browsers -->**/
        $('#' + fieldId + 'Dropdown').parent().removeClass('open');
        var li = $(e.target).closest('li');
        var isoCode = li.attr('rel');
        var phoneCode = li.attr('data-phoneCode');
        var countryName = li.attr('data-countryName');
        var title = opt_callback ? opt_callback(isoCode, phoneCode, countryName) : li.find('a').html();
        $('#' + fieldId).val(isoCode);
        $('#' + fieldId + 'Title').html(title);
    });
    var field = $('#' + fieldId);
    var isSelect = field[0].tagName.toLowerCase() == 'select';
    if (isSelect && opt_callback) {
        field.on('change', function(sel, e) {
            var option = $("option:selected", this);
            var isoCode = option.attr('value');
            var phoneCode = option.attr('data-phoneCode');
            var countryName = option.attr('data-countryName');
            opt_callback(isoCode, phoneCode, countryName);
        });
    }
    // Set current value for bootstrap dropdown.
    var preloadValue = field.val();
    if (!isSelect && preloadValue) {
        var li = $('#' + fieldId + 'Dropdown li[rel=' + preloadValue + ']');
        var fieldTitle = opt_callback ? opt_callback(li) : li.find('a').html();
        $('#' + fieldId + 'Title').html(fieldTitle);
    }
};
