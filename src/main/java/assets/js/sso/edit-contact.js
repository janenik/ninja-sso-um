/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/setup-dropdown.js" />

(function() {
    /**<#-- Country. -->*/
    var setUpDropdown = sso.htmlHelpers.setUpDropdown;
    setUpDropdown('countryId', function(li) {
            var phone = $('#phone');
            if (phone.val().length < 5) {
                $('#phone').val('+' + li.attr('rel2') + ' ');
                $('#phone')[0].focus();
                $('#phone')[0].selectionStart = $('#phone')[0].selectionEnd = $('#phone').val().length;
            }
            return li.attr('rel3');
        });

    $('#editContactForm').submit(function() {
        $('#editContactSubmit').attr('disabled', 'disabled');
        $('#editContactSubmit').html('${i18n("commonSaving")}');
    });
 })();
