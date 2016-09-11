/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/setupdropdown.js" />

(function() {
    /**<#-- Birthday month, gender, country. -->*/
    var setUpDropdown = sso.htmlHelpers.setUpDropdown;
    setUpDropdown('birthMonth');
    setUpDropdown('gender');

    $('#editPersonalForm').submit(function() {
        $('#editPersonalSubmit').attr('disabled', 'disabled');
        $('#editPersonalSubmit').html('${i18n("commonSaving")}');
    });
 })();
