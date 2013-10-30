/*************************************************
 *  Classes Definition for spiritmobile LocalSchedule
 *  
 */

var LocalStorage = {
    
    isAvailable: function() {
        if (typeof(localStorage) == 'undefined') {
            return false;
        }
        else return true;
    },
    
    createPopUp: function(e) {
        
        var wdth = $('body').width();
        var hght = $('body').height();
        var popup = $('<div class="ui-loader ui-overlay-shadow ui-corner-all ui-body-a pop in"></div>');
        popup.css({
            'width': (wdth/2), 
            'height': (hght/4)- '20%', 
            'margin-left': '0px', 
            'padding': '10px 0px'
        });
        
        var pop_w = popup.width();
        var pop_h = popup.height();
        
        var lft = ((wdth/2) - (pop_w/2));
        var tp = ((hght/2) - (pop_h)/2);
        
        var viewportwidth = window.innerWidth;
        var viewportheight = window.innerHeight;
        
        var newtop = (window.pageYOffset + (viewportheight/2)) - (pop_h/2);
        
        popup.css({
            'display': 'block', 
            'top': newtop, 
            'left': lft
        });
        
        var buttonyes = $('<a href="#" data-role="button">Yes</a>');
        buttonyes.buttonMarkup({
            theme: "a"
        });
        buttonyes.click(function() {
            // TODO: Implement what to do, if popup answer = yes
            //alert(e.data.day + ':\n ' +  e.data.timeslot + ':\n ' + e.data.strdata);
            
            // Insert into LocalStorage
            e.data.lS.storeEvent(e.data.day, e.data.timeslot, e.data.strdata);
            
            popup.remove();
        });
        
        
        var buttonno = $('<a href="#" data-role="button">No</a>');
        buttonno.buttonMarkup({
            theme: "a"
        });        
        buttonno.click(function() {
            popup.remove();
        });
        
        
        var btn1 = $('<div align="center" class="ui-block-a"></div>').append(buttonyes);
        var btn2 = $('<div align="center" class="ui-block-b"></div>').append(buttonno);
        
        var popupheader = $('<div align="center"><h1>Add To MySchedule?</h1></div>');
        var buttongroup = $('<div class="ui-grid-a"></div>');
        buttongroup.append(btn1);
        buttongroup.append(btn2);
        
        popup.append(popupheader);
        popup.append(buttongroup);
        popup.appendTo('body');
    },
    
    addStorageAbilityTo: function(lS) {
        
        var schedule_count = $('#scheds li').size();
        var sched_name = $('#scheds li');
        
        var i=0;
        $.each(sched_name, function() {
        
            var the_a = $(sched_name[i]).find('a');
            var name = the_a.text();
            
            for (var j=0; j<5; j++) {
                for (var k=0; k<7; k++) {
                    var sstr = '#schedulePage-' + name + '-' + j + ' #timeslot-' + k + ' #eventcontainer';
                    var child_counter = $($(sstr).children()).size();

                    if (child_counter > 0) {
                        
                        var storagedata = $(sstr).html();
                        $('#schedulePage-' + name + '-' + j + ' #timeslot-' + k + ' a').bind('click', {strdata: storagedata, timeslot: getRealTimeSlot(k), day: getDay(j), lS: lS}, LocalStorage.createPopUp);
                    }

                }
            }
            
            i++;
        });
        
    }
    
};


var LocalSchedule = function() {
    
    var index = -1;
    
    return {
        /**
         * @description dataStructure contains an event
         * @return Object with following Components:
         *         day: {String}
         *         timeslot: {String}
         *         data: {String}
         *         id: {Number}
         */
        dataStructure: function(day, timeslot, data) {
            
            index++;
            return {
                m_day: day,
                m_timeslot: timeslot,
                m_data: data,
                m_id: index
            }
            
        },
        
        /**
         * @description initialize the LocalSchedule
         */
        init: function() {
            
            /**
             * @description inline function, that creates the pages of the 
             *              local schedule 
             */
            var CreateLocalSchedulePages = function () {
                
                var back_str;
                var next_str;
                
                for (var i=0; i<5; i++) {
                    
                    if (i<1) 
                        back_str = '#p1'
                    else 
                        back_str = '#localSchedule-' + getDay(i-1);
                    if (i>=4) 
                        next_str = ''
                    else
                        next_str = '#localSchedule-' + getDay(i+1);
                    
                    $('body').append(
                        '<div data-role="page" id="localSchedule-' + getDay(i) + '" data-url="localSchedule-' + getDay(i) + '">'                    +
                            '<div data-role="header">'                                      + 
                                '<h1>' + getDay(i) + '</h1>'                                +
                                '<a class="button" href="' + back_str + '">Back</a>'        +
                                '<a class="button" href="' + next_str + '">Next</a>'        +
                            '</div>'                                                        +
                            '<div data-role="content">'                                     +
                                '<ul data-role="listview" class="ui-listview" id="schedulelist-'+ getDay(i) +'">'                    +
                                '</ul>'                                                     +
                            '</div>'                                                        +
                        '</div>'    
                    );
                            
                    if (next_str == '') {
                        $('#localSchedule-' + getDay(4) + ' a').remove(':contains("Next")');
                    }
                    
                    for (var j=0; j<7; j++) {
                        $('#schedulelist-' + getDay(i)).append(
                            '<li id="timeslot-' + j + '">'                                  +
                                '<table width="100%"><tr>'                                  + 
                                    '<td width="50px">' + getTimeSlot(j) +'</td>'           +
                                    '<td id="ls-eventcontainer"></td>'           +
                                '</tr></table>'                                             +
                            '</li>'
                        );
                        $('#schedulelist-' + getDay(i) + ' #timeslot-' + j + ' #ls-eventcontainer').bind('click', {day: getDay(i), timeslot: getRealTimeSlot(j)}, removeEvent);    
                    }
                    
                    
                    
                }
                
            }
            
            CreateLocalSchedulePages();
            
        },
        
        // TODO: Implement storeEvent()
        storeEvent: function(day, timeslot, eventdata) {
            var ev = new this.dataStructure(day, timeslot, eventdata);
            
            
            window.localStorage.setItem(index, JSON.stringify(ev));
        },
        
        // TODO: Implement loadEvent()
        loadEvent: function() {
            
            for (var i=0; i<window.localStorage.length; i++) {
                var t_item = JSON.parse(window.localStorage.getItem(i)); 
                $('#localSchedule-' + t_item.m_day + ' #timeslot-' + getSlotNumberFromTimeSlot(t_item.m_timeslot) + ' #ls-eventcontainer').children().remove();
                $('#localSchedule-' + t_item.m_day + ' #timeslot-' + getSlotNumberFromTimeSlot(t_item.m_timeslot) + ' #ls-eventcontainer').append(t_item.m_data);
            }
        },
        
        removeAll: function() {
            window.localStorage.clear();
        }
        
        
        
    };
    
};

function removeEvent(e) {
    var len = window.localStorage.length;
    var key_number;
    var item;
    for (var f=0; f<len; f++) {
        key_number = window.localStorage.key(f);
        item = window.localStorage.getItem(key_number);
        if (item != null)
            if ((JSON.parse(item)).m_day == e.data.day && (JSON.parse(item)).m_timeslot == e.data.timeslot) {
                    window.localStorage.removeItem(key_number);
                    //$('#localSchedule-' + t_item.m_day + ' #timeslot-' + getSlotNumberFromTimeSlot(t_item.m_timeslot) + ' #ls-eventcontainer').children().remove();
                    $('#localSchedule-' + e.data.day + ' #timeslot-' + getSlotNumberFromTimeSlot(e.data.timeslot) + ' #ls-eventcontainer').children().remove();
                    break;
                }
    }
    
//    var item;
//    for (var i=0; i<len; i++) {
//        item = window.localStorage.getItem(i);
//        if (item != null)
//            if ((JSON.parse(item)).m_day == e.data.day && (JSON.parse(item)).m_timeslot == e.data.timeslot) {
//                window.localStorage.removeItem(i);
//                //$('#localSchedule-' + t_item.m_day + ' #timeslot-' + getSlotNumberFromTimeSlot(t_item.m_timeslot) + ' #ls-eventcontainer').children().remove();
//                $('#localSchedule-' + e.data.day + ' #timeslot-' + getSlotNumberFromTimeSlot(e.data.timeslot) + ' #ls-eventcontainer').children().remove();
//            }
//    }
}