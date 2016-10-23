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

    function backToAllEvents(e) {
        $(userEventsSearch).show();
        $(userEventView).hide();
        e.preventDefault();
        return false;
    }

    function moveToNextEvent(e) {
        if (nextDataEventId) displayUserEvent(nextDataEventId);
        e.preventDefault();
        return false;
    }

    function moveToPrevEvent(e) {
        if (prevDataEventId) displayUserEvent(prevDataEventId);
        e.preventDefault();
        return false;
    }

    $('#buttonBackToEvents').on('click touchend', backToAllEvents);
    $('#buttonPrevEvent').on('click touchend', moveToPrevEvent);
    $('#buttonNextEvent').on('click touchend', moveToNextEvent);

    $(document).keyup(function(e) {
        switch(e.which) {
            case 37:
                moveToPrevEvent(e);
            break;
            case 38:
                backToAllEvents(e);
            break;
            case 39:
                moveToNextEvent(e);
            break;
        }
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