/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
(function() {
    var userEventsSearch = document.getElementById('userEventsSearch');
    var userEventView = document.getElementById('userEventView');
    $('#' + userEventsSearch.id + ' table tr').on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        /** TODO: show event component **/
    });
 })();