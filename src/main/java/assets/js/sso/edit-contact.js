/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/setup-dropdown.js" />

(function() {
    /**<#-- Country. -->*/
    var setUpDropdown = sso.htmlHelpers.setUpDropdown;
    setUpDropdown('countryId', function(iso, phoneCode, countryName) {
            var phone = $('#phone');
            if (phone.val().length < 7) {
                phone.val('+' + phoneCode + ' ');
                phone[0].focus();
                phone[0].selectionStart = phone[0].selectionEnd = phone.val().length;
            }
            return countryName;
        });

    $('#editContactForm').submit(function() {
        $('#editContactSubmit').attr('disabled', 'disabled');
        $('#editContactSubmit').html('${i18n("commonSaving")}');
    });
 })();
