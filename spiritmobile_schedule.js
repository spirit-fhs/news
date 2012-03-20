/*************************************************
 *  Classes Definition for spiritmobile Timetable
 *  
 */

/**
 * @description containing DegreeCourse Object 
 */
var DegreeCourse = function(degreecourse) {
    
    var course = null;
    
    if (typeof(degreecourse) == 'string') {
        course = degreecourse;
    
        return {

            getDegreeCourse:  function() {
                return course;
            }

        };
    }
    else {
        return null;
    }
};

var AllDegreeCourses = function() {
    
    var degreeCourses = [];
    
    return {
        
        addDegreeCourse: function(degreecourse) {
            if (typeof(degreecourse) == 'object') {
                degreeCourses.push(degreecourse);
            }
        },
        
        getDegreeCourseByName: function(name) {
            if (typeof(name) == 'string') {
                for(var i=0; i<degreeCourses.length; i++) {
                    if (degreeCourses[i].getDegreeCourse() == name) {
                        return degreeCourses[i].getDegreeCourse();
                    }
                }
            }
            return null;
        },
        
        getAllDegreeCourses: function() {
            return degreeCourses;
        }
        
    };
    
};

/**
 *  Definition of Schedule-Structure
 */

var Datapair_v2 = function() {
        
        return {

            key: null,
            value: null,

            getKey: function() {
                return this.key;
            },

            getValue: function() {
                return this.value;
            },
            
            setKey: function(k) {
                this.key = k;
            },
            
            setValue: function(v) {
                this.value = v;
            }

        };
    
};

var Subelement = function() {
    
    return {
        
        elementdata: new Array(0),
        
        addElementData: function(dataelement) {
            this.elementdata.push(dataelement);
        },
        
        getElementData: function() {
            return this.elementdata;
        }
        
    };
    
};

var DataElement = function() {
    
    return {
        
        Elements: new Array(),
        
        addDataElement: function(elem) {
            this.Elements.push(elem);
        },
        
        getDataElementByIndex: function(index) {
            for (var k=0; k<this.Elements.length; k++) {
                if ((k+1) == index) {
                    return this.Elements[k];
                }
                else return null;
            }
        }
        
    };
    
};




// TODO: Define JSONProcessor Class --> FIXED
var JSONProcessor = function() {
    
    var json_source;
    var tmp_Data = [];
    
    
    return {
        
        setJSONSource: function(src) {
            if (typeof(src) == 'string') {
                json_source = src;
                return true;
            }
            else return false;
        },
        
        getJSONSource: function() {
            return json_source;
        },
        
        // Loads Data From a JSON Source
        loadDataFromJSONSource: function() {
            
            var Loading = {
                
                loadJSON: function(tmp_arr) {
                    
                    // Öffne JSON-Quelle
                    $.getJSON(json_source, function(data){
                        var i = 0;
                        
                        // Öffne Stundenplan-Array
                        $.each(data, function(){
                            
                            var tmp_subelement = new Subelement();
                            
                            // Für jedes Schedule-Event ...
                            $.each(data[i], function(key, val){
                                
                                var elem = Loading.saveData(key, val);
                                tmp_subelement.addElementData( elem );
                                
                            });
                            i++;
                            tmp_arr.push(tmp_subelement);
                        });
                        
                    });
                    
                },
                
                //TODO: Implement saveData function (copy inside loadDataFromJSONSource) --> FIXED
                saveData: function(k, v) {

                    

                    //checke Value aus Datenpaar (isArray, isObject, isPrimitive)

                    if ( this.isTypeOf(v) == 'primitive') {
                        // value == primitive(string) Speichere Key-ValuePaar
                        var tmp_datapair = new Datapair_v2();
                        tmp_datapair.setKey(k);
                        tmp_datapair.setValue(v);
                        
                        // gebe Datenpaar zurück
                        return tmp_datapair;
                    }
                    else {
                        if (this.isTypeOf(v) == 'array') {
                           // value == Array -> Speicher Alle Datenpaare des JSON-Arrays in neuem Array ab und gib es zurück
                           var tmp_datapair2 = new Datapair_v2();
                           tmp_datapair2.setKey(k);
                           if (v.length > 0) {
                               var i=0;
                               var tmp_array = [v.length];
                               $.each(v[i], function(key, val) {
                                   //TODO: Checkout saveData() - saving Array !!! --> FIXED
                                   
                                   // Für jedes Element im Array: 
                                   // erzeuge neues Objekt und speichere alle Daten in diesem in diesem Objekt
                                   
                                    if (typeof(tmp_array[i]) != 'object') {
                                        tmp_array[i] = new DataElement();
                                    }
                                    tmp_array[i].addDataElement(Loading.saveData(key, val));
                                    
                                   
                               });
                               
                               i++;
                               tmp_datapair2.setValue(tmp_array);
                               return tmp_datapair2;
                           }
                           else  {
                               tmp_datapair2.setValue(new Array(0));
                               return tmp_datapair2;
                           }
                        }
                        else {
                            if (this.isTypeOf(v) == 'object') {
                                // value == Object -> Erstelle Datenelement und 
                                // speichere alle inneren Elemente darin, dann gib das Datenelement zurück
                                
                                var tmp_datapair3 = new Datapair_v2();
                                tmp_datapair3.setKey(k);
                                
                                var tmp_dataelement = new DataElement();
                                $.each(v, function(key, val) {
                                    // TODO: Checkout saveData() - saving Object !!! --> FIXED
                                    tmp_dataelement.addDataElement(Loading.saveData(key, val));
                                    
                                });
                                tmp_datapair3.setValue(tmp_dataelement);
                                return tmp_datapair3;
                            }
                        }
                    }  
                },
                
                // copy inside loadDataFromJSONSOurce
                isTypeOf: function(variable) {
                    if (typeof(variable) == 'object') {
                        if (variable.length >= 0) {
                            return 'array';
                        } 
                        else {
                           return 'object';
                        }
                    }
                    else {
                        return 'primitive';
                    }

                }
                
            }
            Loading.loadJSON(tmp_Data);
            return tmp_Data;
        },
        
        exportData: function() {
            
            return tmp_Data;
            
        }
        
        
        
    };
      
    
};



var Schedule = function() {
    
    var alldegreecourses = new AllDegreeCourses();
    
    
    return {
        
        schedules: new Array(),
        
        
        addDegreeCourse: function(degreeCourse) {
            if (typeof(degreeCourse) == 'string')
                alldegreecourses.addDegreeCourse( new DegreeCourse(degreeCourse) );
        },
        
        checkDegreeCourseAvailability: function(course) {
            if (alldegreecourses.getDegreeCourseByName(course) != null) {
                return true;
            }
            return false;
        },
        
        addDegreeCoursesToDropdownMenu: function() {
            for (var i=0; i<alldegreecourses.getAllDegreeCourses().length; i++)
                $('#dropdown_menu').append('<option value="' + alldegreecourses.getAllDegreeCourses()[i].getDegreeCourse() + 
                                           '" >' + alldegreecourses.getAllDegreeCourses()[i].getDegreeCourse() + '</option>');
        },
        
        createDegreeCourseDropDownMenuOnElement: function(element) {
            $(element).append('<select id="dropdown_menu" name="classes" title="classes"></select>');
            this.addDegreeCoursesToDropdownMenu();
        },
        
        //TODO: Implement Schedule.loadScheduleData() --> FIXED
        loadScheduleData: function(jsonsource) {
            var jsonproc = new JSONProcessor();
            var schedule_datapair = new Datapair_v2();
            
            jsonproc.setJSONSource(jsonsource);
            
            schedule_datapair.setKey(this.schedules.length);
            schedule_datapair.setValue(jsonproc.loadDataFromJSONSource());
            
            this.schedules.push(schedule_datapair);            
        },
        
        //TODO: Implement getScheduleByIndex() --> FIXED
        getScheduleByIndex: function(idx) {
            for (var i=0; i<this.schedules.length; i++) {
                if (i == idx)
                    return this.schedules[i];
            }
            return null;
        },
        
        //TODO: Filter events by Day & Timeslot --> FIXED
        getEventsByDayByTime: function(schedule, day, time) {
            
            var SearchFunctions = {
            
                searchString: "",
                
                getEventsByDayFromSchedule: function() {
                    
                    function checkEventDayTime(event) {
                        
                        var elemdata = event.getElementData();
                        
                        // mache rekursive Suche nach Key == 'day'
                        // wenn gefunden && Value == day, dann gib event zurück
                        // ansonsten gib nichts zurück
                        for (var i=0; i<elemdata.length; i++) {
                            //old: if (SearchFunctions.doRecursiveSearch(elemdata[i], 'day') == day) {
                            var tmp_day = SearchFunctions.doRecursiveSearch(elemdata[i].getKey(), elemdata[i].getValue(), 'day');
                            var tmp_time = SearchFunctions.doRecursiveSearch(elemdata[i].getKey(), elemdata[i].getValue(), 'time');
                            
                            if (tmp_time == time && tmp_day == day) {
                                SearchFunctions.searchString = "";
                                return true;
                            }
                        }
                         
                    };
                    
                    var events = [];
                    
                    for (i=0; i<schedule.length; i++) {
                        
                        // ist das event eines bestimmten Tages, dann füge es 
                        // zur Liste der richtigen Events hinzu
                        if (checkEventDayTime(schedule[i]) == true) {
                            events.push(schedule[i]);
                        }
                        
                    }
                    
                    return events;
                },
                
                //TODO: Change parameters (datapair, mykey) into (key, val, mykey) --> FIXED
                //old: doRecursiveSearch: function(datapair, mykey) {
                doRecursiveSearch: function(key, val, mykey) {
                    
                    if (SearchFunctions.isTypeOf(val) == 'primitive') {
                        if (key == mykey) {
                            SearchFunctions.searchString = val;
                            return val;
                        }
                    }
                    else {
                        if (SearchFunctions.isTypeOf(val) == 'array') {
                            var elems = val;
                            for (var i=0; i<elems.length; i++) {
                                return SearchFunctions.doRecursiveSearch(key, elems[i], mykey);
                            }
                        }
                        else {
                            if (SearchFunctions.isTypeOf(val) == 'object') {
                                var objs = val.Elements;
                                for (var j=0; j<objs.length; j++) {
                                    var tst = SearchFunctions.doRecursiveSearch(objs[j].getKey(), objs[j].getValue(), mykey);
                                    if (tst == time) 
                                        return time;
                                    if (tst == day)
                                        return day;
                                }
                            }
                        }
                    }
                    
                },
                
                isTypeOf: function(variable) {
                    if (typeof(variable) == 'object') {
                        if (variable.length >= 0) {
                            return 'array';
                        } 
                        else {
                           return 'object';
                        }
                    }
                    else {
                        return 'primitive';
                    }

                }
            
            };
            
            // dayEvents contains all events of a day, which are at a specific time
            var daytimeEvents = SearchFunctions.getEventsByDayFromSchedule();            
            var fuck = "abc";
            
            return daytimeEvents;
            
        },   
        
        getScheduleName: function(schedule) {
        
            return schedule[0].getElementData()[4].getValue();
        
        },
        
        //TODO: Declare & Implement getValueFromKey(event, key) --> FIXED
        getValueFromKey: function(event, key) {
            
            var isTypeOf = function(variable) {
                
                if (typeof(variable) == 'object') {
                        if (variable.length >= 0) {
                            return 'array';
                        } 
                        else {
                           return 'object';
                        }
                    }
                    else {
                        return 'primitive';
                    }
                
            };
            
            var doSearch = function (k, v) {
                
                if (isTypeOf(v) == 'primitive') {
                    if (key == k) {
                        return v;
                    }
                    else return 'novalue';
                }
                else {
                    if (isTypeOf(v) == 'array') {
                        for (var i=0; i<v.length; i++) {
                            var tmp_v = doSearch(k, v[i]);
                            if (typeof(tmp_v) == 'string' && tmp_v != 'novalue') {
                                return tmp_v;
                            }
                            else {
                                return 'novalue';
                            }
                        }
                    }
                    else {
                        if (isTypeOf(v) == 'object') {
                            var len = v.Elements.length;
                            for (var j=0; j<len; j++) {
                                var t_k = v.Elements[j].getKey();
                                var t_v = v.Elements[j].getValue();                                
                                var wert = doSearch(t_k, t_v);
                                
                                if (typeof(wert) == 'string' && wert != 'novalue') {
                                    return wert;
                                }
                                else {
                                    //
                                }
                                
                            }
                        }
                    }
                }
                
            };
            
            var ln = event.getElementData().length;
            for (var i=0; i<ln; i++) {
                if (i == 7) {
                    var sladfgsdf = '"§$%';
                }
                var val = doSearch(event.getElementData()[i].getKey(), event.getElementData()[i].getValue());
                if ((isTypeOf(val) == 'primitive' && val != 'novalue' && val != null)) {
                    return val;
                }
            }
            
        },
        
        //TODO: Implement createSchedulePage(...) & Refactor it to PageStructure-Creation        
        createStructureOfSchedulePages: function(schedule) {
        
        
            //Creating empty Table ==> 5 empty SchedulePages (PageStructure-Creation
 
            var sched_name = this.getScheduleName(schedule);
            var back_str;
            var next_str;
 
            for (var i=0; i<5; i++) {
                
                if (i<1) {
                    back_str = '#p3'
                }
                else {
                    back_str = '#schedulePage-' + sched_name + '-' + (i - 1)
                }
                
                if (i>=4) {
                    next_str = '';
                    
                }
                else {
                    next_str = '#schedulePage-' + sched_name + '-' + (i+1);
                }
                
                // Erstelle SchedulePageGerüst eines Tages
                $('body').append('<div data-role="page" id="schedulePage-' + sched_name + '-' + i + '"></div>');
                $('#schedulePage-' + sched_name + '-' + i).append(
                    '<div data-role="header">'                                             + 
                        '<h1>' + getDay(i) + '</h1>'           +
                        '<a class="button" href="' + back_str + '">Back</a>'      +
                        '<a class="button" href="' + next_str + '">Next</a>'      +
                    '</div>'                                                            +
                    '<div data-role="content">'                                                +
                        '<ul data-role="listview" data-inset="true" id="scheduleDay-' + getDay(i) + '"></ul>' +
                    '</div>'
                );
                    
                // Löscht den Next Button, sobald die letzte Seite erreicht wurde
                if (next_str == '') {
                    $('#schedulePage-'+ sched_name + '-' + 4 + ' a').remove(':contains("Next")');
                }
                    
                // Erstelle Tagesansicht
                for (var j=0;j<7;j++) {
                    
                    // function for getting all the events depending on day & timeslot
                    // var daytimeEvents = this.getEventsByDayByTime(schedule, getDay(i), getTimeSlot(j));
                    
                    // Erstelle AddToMyTable-Option
//                    var dialog = 
                    
                    // Erstelle Zeitslots für jeden Tag
                    $('#schedulePage-' + sched_name + '-' + i + ' #scheduleDay-' + getDay(i)).append(
                         
                        '<li id="timeslot-' + j + '">'                                  +
                            '<a data-rel="dialog" data-transition="pop">' +
                                '<table width="100%"><tr>'                                  + 
                                    '<td width="50px">' + getTimeSlot(j) +'</td>'            +
                                    '<td id="eventcontainer"></td>'                   +
                                '</tr></table>'                                             +
                            '</a>' + 
                        '</li>'
                    );
                        
                }   
                
                var fuck = "abc";
            }
            
            this.fillSchedulePages(schedule);
            
        },
        
        //TODO: Implement fillSchedulePage() 
        fillSchedulePages: function(schedule) {
            
            var page;
            var sched_name = this.getScheduleName(schedule);
            
            
            // für jeden Tag
            for (var i=0; i<5; i++) {
                
                // für jeden Timeslot
                for (var j=0; j<7; j++) {
            
                    // Hole Events des Timeslots
                    var daytimeEvents = this.getEventsByDayByTime(schedule, getDay(i), getRealTimeSlot(j));
            
                    // gibt es mehrere Events?
                    if (daytimeEvents.length > 1) {
                        
                        // ja, dann füge alle nacheinander ein
                        for (var k=0; k<daytimeEvents.length; k++) {
                            var event_type = this.getValueFromKey(daytimeEvents[k], 'eventType');
                            var event_lesson = this.getValueFromKey(daytimeEvents[k], 'titleShort');
                            var event_group = this.getValueFromKey(daytimeEvents[k], 'group');
                            var event_building = this.getValueFromKey(daytimeEvents[k], 'building');
                            var event_room = this.getValueFromKey(daytimeEvents[k], 'room');
                            var event_weekly = this.getValueFromKey(daytimeEvents[k], 'week');
                            var event_owner = this.getValueFromKey(daytimeEvents[k], 'name');
                            var event_icon;
                            var event_week_type;
                            
                            if (event_type == 'Vorlesung') 
                                event_icon = 'images/lecture.png'
                            else if (event_type == 'Uebung')
                                event_icon = 'images/uebung.png';
                            
                            if (event_weekly == 'w')
                                event_week_type = 'event_weekly'
                            else if (event_weekly == 'g')
                                event_week_type = 'event_even'
                            else if (event_weekly == 'u')
                                event_week_type = 'event_odd';
                            
                            $('#schedulePage-' + sched_name + '-' + i + ' #timeslot-' + j + ' #eventcontainer').append(
                                // Füge Events des Timeslots[j] an Tag[i] hier ein
                                // TEST: Einträge hier nur zum testen da -> Einträge generieren!!
                                
//                                '<div id="event-' + k + '">' + 
//                                    this.getValueFromKey(daytimeEvents[k], 'titleShort') + ' ' + 
//                                    '(' + this.getValueFromKey(daytimeEvents[k], 'week') + ')' +
//                                    '  -  Prof: ' + this.getValueFromKey(daytimeEvents[k], 'name') +
//                                    '  -  Gebäude: ' + this.getValueFromKey(daytimeEvents[k], 'building') +
//                                    '  -  Raum: ' + this.getValueFromKey(daytimeEvents[k], 'room') +
//                                '</div>'

                                // EventBlock:
                                 
                                $('<div id="event-' + k + '" class="event_borders ' + event_week_type + '"></div>')
                                    .append($('<div>').append(event_lesson))
                                    .append($('<div class="event_icon_pos">').append('<img src="' + event_icon + '"/>'))
                                    .append($('<div>').append('Gruppe: ' + event_group))
                                    .append($('<div>').append(event_building + ': ' + event_room))
                                    .append($('<div class="event_owner_pos">').append(event_owner)),
                                    
                                $('<div style="height: 15px"></div>')

                            );
                        }
                        
                    }
                    else {
                        if (daytimeEvents.length > 0) {
                            event_type = this.getValueFromKey(daytimeEvents[0], 'eventType');
                            event_lesson = this.getValueFromKey(daytimeEvents[0], 'titleShort');
                            event_group = this.getValueFromKey(daytimeEvents[0], 'group');
                            event_building = this.getValueFromKey(daytimeEvents[0], 'building');
                            event_room = this.getValueFromKey(daytimeEvents[0], 'room');
                            event_weekly = this.getValueFromKey(daytimeEvents[0], 'week');
                            event_owner = this.getValueFromKey(daytimeEvents[0], 'name');
                            event_icon;
                            event_week_type;
                            
                            if (event_type == 'Vorlesung') 
                                event_icon = 'images/lecture.png'
                            else if (event_type == 'Uebung')
                                event_icon = 'images/uebung.png';
                            
                            if (event_weekly == 'w')
                                event_week_type = 'event_weekly'
                            else if (event_weekly == 'g')
                                event_week_type = 'event_even'
                            else if (event_weekly == 'u')
                                event_week_type = 'event_odd';
                            
                            // nein, dann füge nur das einzelne ein
                            
                            $('#schedulePage-' + sched_name + '-' + i + ' #timeslot-' + j + ' #eventcontainer').append(
                            
                                
                                // Füge Events des Timeslots[j] an Tag[i] hier ein
                                // TEST: Einträge hier nur zum testen da -> Einträge generieren!!
                                
//                                '<div id="event-0">' + 
//                                    this.getValueFromKey(daytimeEvents[0], 'titleShort') + ' ' +
//                                    '(' + this.getValueFromKey(daytimeEvents[0], 'week') + ')' +
//                                    '  -  Prof: ' + this.getValueFromKey(daytimeEvents[0], 'name') +
//                                    '  -  Gebäude: ' + this.getValueFromKey(daytimeEvents[0], 'building') +
//                                    '  -  Raum: ' + this.getValueFromKey(daytimeEvents[0], 'room') +
//                                '</div>'

                                $('<div id="event-' + k + '" class="event_borders ' + event_week_type + '"></div>')
                                    .append($('<div>').append(event_lesson))
                                    .append($('<div class="event_icon_pos">').append('<img src="' + event_icon + '"/>'))
                                    .append($('<div>').append('Gruppe: ' + event_group))
                                    .append($('<div>').append(event_building + ': ' + event_room))
                                    .append($('<div class="event_owner_pos">').append(event_owner))
                            );
                        }
                    }
            
                    
                    
                }

            }
            
            
        }
        
        
        
    };
    
};



/**
 * helperfunctions
 */
function isArrayOrObject(obj) {
    
    if (typeof(obj) == 'object') 
        if(obj.length) 
            return 'array';
        else return 'object';
    else return null;
};

  
function getDay(nb) {
    switch(nb) {
        case 0:return 'Montag';
        case 1:return 'Dienstag';
        case 2:return 'Mittwoch';
        case 3:return 'Donnerstag';
        case 4:return 'Freitag';
    }
};

function getRealTimeSlot(nb) {
    switch(nb) {
        case 0:return '08.15-09.45';
        case 1:return '10.00-11.30';
        case 2:return '11.45-13.15';
        case 3:return '14.15-15.45';
        case 4:return '16.00-17.30';
        case 5:return '17.45-19.15';
        case 6:return '19.30-21.00';
    }
};

function getTimeSlot(nb) {
    switch(nb) {
        case 0:return '08.15-<br>09.45';
        case 1:return '10.00-<br>11.30';
        case 2:return '11.45-<br>13.15';
        case 3:return '14.15-<br>15.45';
        case 4:return '16.00-<br>17.30';
        case 5:return '17.45-<br>19.15';
        case 6:return '19.30-<br>21.00';
    }
};

function getSlotNumberFromTimeSlot(slotname) {
    if (slotname == '08.15-09.45') {
        return 0;
    }
    else if(slotname == '10.00-11.30') {
        return 1;
    }
    else if(slotname == '11.45-13.15') {
        return 2;
    }
    else if(slotname == '14.15-15.45') {
        return 3;
    }
    else if(slotname == '16.00-17.30') {
        return 4;
    }
    else if(slotname == '17.45-19.15') {
        return 5;
    }
    else if(slotname == '19.30-21.00') {
        return 6;
    }
}
