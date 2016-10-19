/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
(function() {
    var userEventsSearch = document.getElementById('userEventsSearch');
    var userEventView = document.getElementById('userEventView');
    var nextDataEventId;
    var prevDataEventId;

    function $eventId(element) {
        return element.length && element.attr('data-event-id');
    }

    function displayUserEvent(eventId) {
        var dataElement = eventId && $('div[data-event-id=' + eventId + ']');
        if (!dataElement || !dataElement.length) {
            return;
        }

        /** <!#-- Handle buttons visibility. --> */
        nextDataEventId = $eventId(dataElement.next());
        prevDataEventId = $eventId(dataElement.prev());

        $('#buttonNextEvent')[0].style.visibility = nextDataEventId ? '' : 'hidden';
        $('#buttonPrevEvent')[0].style.visibility = prevDataEventId ? '' : 'hidden';

        /** <!#-- Populate the data. --> */
        var userTargetId = dataElement.attr('data-event-target-id');
        var userData = userTargetId + ' / ' + dataElement.attr('data-event-target-username');
        $('#userEventTarget').text(userTargetId ? userData : '');

        $('#userEventId').text(eventId);
        $('#userEventType').text(dataElement.attr('data-event-type'));
        $('#userEventUrl').text(dataElement.attr('data-event-url'));
        $('#userEventIP').text(dataElement.attr('data-event-ip'));
        $('#userEventTime').text(dataElement.attr('data-event-time'));

        var dataJson = JSON.parse($(dataElement).html());
        var details = dataJson['event.data'];
        delete dataJson['event.data'];
        $('#userEventData').text(JSON.stringify(dataJson, null, '  '));
        $('#userEventDataDetails').text(JSON.stringify(details, null, '  '));

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

    /** <#-- Table icon handler. --> */
    $('#' + userEventsSearch.id + ' table tr .btn-event-view').on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var eventId = $eventId($(e.target).closest('tr'));
        displayUserEvent(eventId);
        return false;
    });

 })();