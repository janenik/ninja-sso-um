/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
(function() {
    var userEventsSearch = document.getElementById('userEventsSearch');
    var userEventView = document.getElementById('userEventView');
    var nextDataEventId;
    var prevDataEventId;

    function displayUserEvent(eventId) {
        var dataElement = $('div[data-event-id=' + eventId + ']');
        dataElement = dataElement.length && dataElement[0] || null;
        if (!dataElement) {
            return;
        }

        /** <!#-- Handle buttons visibility. --> */
        var nextDataElement = $(dataElement).next();
        nextDataEventId = nextDataElement.length && nextDataElement[0].getAttribute('data-event-id');

        var prevDataElement = $(dataElement).prev();
        prevDataEventId = prevDataElement.length && prevDataElement[0].getAttribute('data-event-id');

        $('#buttonNextEvent')[0].style.visibility = nextDataEventId ? '' : 'hidden';
        $('#buttonPrevEvent')[0].style.visibility = prevDataEventId ? '' : 'hidden';

        /** <!#-- Populate the data. --> */
        var userTargetId = dataElement.getAttribute('data-event-target-id');
        var userData = userTargetId + ' / ' + dataElement.getAttribute('data-event-target-username');
        $('#userEventTarget').text(userTargetId ? userData : '');

        $('#userEventId').text(eventId);
        $('#userEventType').text(dataElement.getAttribute('data-event-type'));
        $('#userEventUrl').text(dataElement.getAttribute('data-event-url'));
        $('#userEventIP').text(dataElement.getAttribute('data-event-ip'));
        $('#userEventTime').text(dataElement.getAttribute('data-event-time'));

        var dataJson = JSON.parse($(dataElement).html());
        var details = dataJson['event.data'];
        delete dataJson['event.data'];
        $('#userEventData').text(JSON.stringify(dataJson));
        $('#userEventDataDetails').text(JSON.stringify(details));

        $(userEventsSearch).hide();
        $(userEventView).show();
    }

    $('#buttonBackToEvents').on('click touchend', function(e) {
        $(userEventsSearch).show();
        $(userEventView).hide();
        return false;
    });

    $('#buttonPrevEvent').on('click touchend', function(e) {
        if (prevDataEventId) displayUserEvent(prevDataEventId);
        return false;
    });

    $('#buttonNextEvent').on('click touchend', function(e) {
         if (nextDataEventId) displayUserEvent(nextDataEventId);
         return false;
    });

    /** <#-- Table row handler. --> */
    $('#' + userEventsSearch.id + ' table tr').on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var tr = $(e.target).closest('tr');
        var eventId = tr.length && tr[0].getAttribute('data-user-event-id') || null;
        displayUserEvent(eventId);
    });

 })();