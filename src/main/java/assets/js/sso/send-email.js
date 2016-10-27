/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/

(function() {
    $('#sendEmailorm').submit(function() {
        $('#endEmailSubmit').attr('disabled', 'disabled');
        $('#endEmailSubmit').html('${i18n("commonSending")}');
    });
 })();
