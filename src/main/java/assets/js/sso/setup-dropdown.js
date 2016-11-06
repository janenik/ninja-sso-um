var sso = sso || {};
sso.htmlHelpers = sso.htmlHelpers || {};

/**
 * <#--
 * Sets up a connection between Bootstrap dropdown and hidden field in form.
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
