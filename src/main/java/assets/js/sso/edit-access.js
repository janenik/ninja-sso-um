/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
<#include "/assets/js/sso/setupdropdown.js" />

(function() {
    /**<#-- Role. -->*/
    var setUpDropdown = sso.htmlHelpers.setUpDropdown;
    setUpDropdown('role');
    setUpDropdown('signInState');
    setUpDropdown('confirmationState');

    $('#editRoleForm').submit(function() {
        $('#editRoleSubmit').attr('disabled', 'disabled');
        $('#editRoleSubmit').html('${i18n("commonSaving")}');
    });
 })();
