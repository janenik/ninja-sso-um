/**<#-- This javascript source is the Fremarker template and supposed to be included. -->*/
(function() {
    $('#signInForm').submit(function() {
        $('#signInSubmit').attr('disabled', 'disabled');
        $('#signInSubmit').html('${i18n("signInButtonTitleLoading")}');
    });
})();