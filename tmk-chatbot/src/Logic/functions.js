//авторизация. это должно измениться позже
function авторизация(value){
     var $session = $jsapi.context().session;
     return true;
}

//Используется для показа списка товаров в понятном виде (таблица) администратору по электронной почте.	
function канцтоварыToString() {
    var $session = $jsapi.context().session;
    
    var text = "<table>";
    for (var i = 0, l = $session.товары.length; i < l; i++){
        text += "<tr><th> " + $session.товары[i].товар + "</th><th> " + $session.товары[i].количество + "<th></tr>";
        
    }
    text += "</table>"
    
    return text;
}

//Используется для показа списка товаров в понятном виде (таблица) пользователю через Телеграм. 
function канцтоварыToStringTelegram() {
    var $session = $jsapi.context().session;
    
    var text = "<br>";
    for (var i = 0, l = $session.товары.length; i < l; i++){
        text += "<br><tr><th> " + $session.товары[i].товар+"</th><th> " + $session.товары[i].количество + "<th></tr></br>";
        
    }
    text += "</br>"
    
    return text;
}

//Он используется для проверки того, что номер телефона предоставленный клиентом, соответствует требованиям. 
//номер должен состоять из 10, 11 или 5 цифр 
function booleanТелефон(value) {
    var $session = $jsapi.context().session;
    
    //12345 913454545
    return (booleanМобильныйТелефон(value)||booleanОфисныйТелефон(value));
}
function booleanМобильныйТелефон(value){
    var $session = $jsapi.context().session;
    if(value.length != 10){
        return false;
    }
    if (value[0] == "8" || value[0] == "9") {
        return booleanNumerArray(value, 1);
    }
    else {return false;}
}
function booleanОфисныйТелефон(value){
     var $session = $jsapi.context().session;
    if(value.length != 5){
        return false;
    }
    else{
        return booleanNumerArray(value, 0);
    }
}
//Вспомогательная функция, которая возвращает true, если value chart является число.
function booleanNumerChert(value) {
    var $session = $jsapi.context().session;
    if (value == "0" || value == "1" || value == "2" || value == "3" || value == "4" || value == "5" 
        ||value == "6" ||value == "7" || value == "8" || value == "9") {
        return true;
    }
    else {return false;}
}
function booleanТелефонOld(value) {
    var $session = $jsapi.context().session;
    
    //12345 913454545
    if (value.length == 5 || value.length == 10) {
        return booleanNumerArray(value, 0);
    }
    //7913454545
    else if (value.length == 11) {
        if(value[0] == "8" || value[0] == "7") {
           return booleanNumerArray(value, 1);
        }
        return false;
    }
    //+7913454545
    else if (value.length == 12) {
        if (value[0] == "+"){
            if (value[1] == "8" || value[1] == "7") {
                return booleanNumerArray(value, 2);
            }
            return false;
        }
        return false;
    }   
    else {return false;}
}

//Вспомогательная функция, которая возвращает true, если value (array) является число.
//Start (integer) определяет, с какой позиции начинается проверка. 
function booleanNumerArray(value,start) {
    for (var i = start, l = value.length; i < l; i++) {
                    if (!booleanNumerChert(value[i])) {return false;}
    }
    return true;
}

//дд,мм,гггг
function textToDate(text){
    var $session = $jsapi.context().session;
    text = replace(text,"/",",");
    text = replace(text,"-",",");
    text = replace(text,"_",",");
    text = text.replace("  "," ");
    text = text.replace(".", ",");
    text = text.replace(".", ",");
    text = text.replace(" ", ",");
    text = text.replace(":", ",");
    text = text.replace(";", ",");
    text = text.split(',');
    
    if(Math.floor(text[0]) > 32 || Math.floor(text[0]) < 1){return false;}
    if(Math.floor(text[1]) > 13 || Math.floor(text[1]) < 1){return false;} 
    $session.дата = new Date(text[2], text[1], text[0]);//yyyy,mm,dd
    return $session.дата;
}
//value string array, b sustitulle a.
function replace(value,a,b){
    
    var text = "";
    for(var i = 0, l = value.length; i < l; i++){
        if(value[i] == a){
            text += b;
        }
        else{
            text +=value[i];
        }
    }
    return text;
}
// hh:mm
function textToTime(text){
    //var $session = $jsapi.context().session;
    
    text = text.replace("  ", ":");
    text = replace(text,"/",":");
    text = replace(text,"-",":");
    text = replace(text,"_",":");
    text = replace(text,",",":");
    text = replace(text,";",":");
    text = replace(text,".",":");
    text = replace(text," ",":");
   
    text = text.split(":");
    var время = "";
    var a = text[0];
    var b = text[1];
    if(a.length > 2 || b.length > 2){
        время = "error";
        return время;
    }
    if(!booleanNumerArray(a, 0) || !booleanNumerArray(b, 0)){
        время = "error";
        return время;
    }
    Number(a);
    Number(b);
    if(a > 23 || b > 59 || a < 0 || b < 0){
        время = "error";
        return время;
    }
    время += a + ":";
    if(b < 10){
        время += "0" + b; 
    }
    else{
        время += b + "";
    } 
    return время;
   
}

//дата больше текущей даты
function проверкаДаты(дата,value){
    var $session = $jsapi.context().session;
    var d = $session.дата.getDate();
    var m = $session.дата.getMonth();
    var y = $session.дата.getFullYear();
    var result = false;
    var time = Date.now();
    var текущаяДата = new Date(time);
    текущаяДата.setDate(текущаяДата.getDate() + value);
    
    if(текущаяДата <= дата)
        result = true;
    $session.дата = new Date(y, m, d);
    return result;
}

//data to string
function dataToText(дата){
    var $session = $jsapi.context().session;
    var t = дата;
    //var t = $session.дата.toString();
    t= t.split("T");
    var s = t[0].split("-");
    var y = new String(s[0]);
    var m = new String(s[1]);
    var d = new String(s[2]);
    var mm = m.replace("0","");
    var dd = d.replace("0","");
    $session.дата = new Date(y, mm-1, dd);
    return d + "-" + m + "-" + y;
}

//Заявка на транспорт
function ЗаявкаНаТранспорт(){
    var $session = $jsapi.context().session;
    //чтобы дать структуру на дата
    var t = $session.дата.split("T");
    var time = t[0].split("-");
    //создать таблицу, чтобы дать структуру
    var text = "";
    text +="<table>"
    text += "<tr> Заявка на транспорт </tr>";
    text += "<tr><td>Организация: </td><td>" + $session.организация + " </td></tr>";
    text += "<tr><td>Подразделение/Дирекция: </td><td>" + $session.Подразделение + " </td></tr>";
    text += "<tr><td>Руководитель: </td><td>" + $session.руководительФИО + " </td></tr>";
   
    
    text += "<tr><td>Ответственный пассажир: </td><td>" + $session.ОтветственныйПассажир + " </td></tr>";
    text += "<tr><td>Телефон ответственного: </td><td>" + $session.ТелефонОтветственного + " </td></tr>";
    
    for(var i = 0, l = $session.пассажиры.length; i < l; i++){
        text += "<tr><td>Пассажир: </td><td>" + $session.пассажиры[i] + " </td></tr>";
    }
    
    text += "<tr><td>Зарезервированная дата: </td><td>" + time[2] + "-" + time[1] + "-" + time[0] + " " + $session.время + " </td></tr>";
    text += "<tr><td>Место подачи (Откуда): </td><td>" + $session.местоПодачи + " </td></tr>";
    text += "<tr><td>Пункт назначения (Куда): </td><td>" + $session.пунктНазначения + " </td></tr>";
    text += "</table>"
    return text;
}

function ЗаявкаНаТранспортРуковадительТелеграм(){
    var $session = $jsapi.context().session;
    var text = "<br>";
    text += "<br><tr><th>Организация: </th><th>" + $session.организация + " </th></tr>";
    text += "<br><tr><th>Подразделение/Дирекция: </th><th>" + $session.Подразделение + " </th></tr>";
    text += "<br><tr><th>Руководитель подразделения: </th><th>" + $session.руководительФИО + " </th></tr>";
    text += "</br>"
    return text;
}

function ЗаявкаНаТранспортТелеграм(){
    var $session = $jsapi.context().session;
    //чтобы дать структуру на дата
    var t = $session.дата.split("T");
    var time = t[0].split("-");
    //создать таблицу, чтобы дать структуру
    var text = "";
    
    text += "<br><tr><th> Заявка на транспорт  </th></tr>";
    text += ЗаявкаНаТранспортРуковадительТелеграм();
    
    
    text += "<br><tr><th>Ответственный пассажир: </th><th>" + $session.ОтветственныйПассажир + " </th></tr>";
    text += "<br><tr><th>Телефон ответственного: </th><th>" + $session.ТелефонОтветственного + " </th></tr>";
    
    for(var i = 0, l = $session.пассажиры.length; i < l; i++){
        text += "<br><tr><th>Пассажир: </th><th>" + $session.пассажиры[i] + " </th></tr>";
    }
    
    text += "<br><tr><th>Зарезервированная дата: </th><th>" + time[2] + "-" + time[1] + "-" + time[0] + " " + $session.время + " </th></tr>";
    text += "<br><tr><th>Место подачи (Откуда): </th><th>" + $session.местоПодачи + " </th></tr>";
    text += "<br><tr><th>Пункт назначения (Куда): </th><th>" + $session.пунктНазначения + " </th></tr>";
    text += "</table>"
    return text;
}

//Биржа идей   delete 
function БиржаИдей(){
    var $session = $jsapi.context().session;
    var text = "<table>";
    text += "<tr><td>Биржа идей </td> </tr>"
    //text += "<tr><td>Имя идеи: </td><td>" + $session.имяСвоюИдею + " </td></tr>";
    //text += "<tr><td> Проект: </td><td>" + $session.проект + " </td></tr>";
    //text += "<tr><td> Суть идеи: </td><td></td></tr>";
    //text += "<tr><td>" + $session.сутьИдеи + "</td></tr>";
    text += "<tr><td>Направление: </td><td>" + $session.направлениеУлучшений + " </td></tr>";
    
    text += "<tr><td> </td></tr>";
    text += "<tr><td>Проблема:</td></tr>";
    text += "<tr><td>" + $session.проблема + "</td></tr>";
    text += "<tr><td> </td></tr>";
    text += "<tr><td>Что сделать:</td></tr>";
    text += "<tr><td>" + $session.чтоСделать + "</td></tr>";
    
    text += "<tr><td> </td></tr>";
    text += "<tr><td>Место применения</td></tr>";
    text += "<tr><td>Предприятие: </td><td>" + $session.местоПримененияПредприятие + " </td></tr>";
    text += "<tr><td>Подразделение: </td><td>" + $session.местоПримененияПодразделение + " </td></tr>";
    text += "<tr><td>Участок/Отдел/Оборудование: </td><td>" + $session.местоПримененияУчасток + " </td></tr>";
    
    text += "<tr><td> </td></tr>";
    text += "<tr><td>Автор</td></tr>";
    text += "<tr><td>Ф.И.О. </td><td>" + $session.авторИмя + " </td></tr>";
    text += "<tr><td>Должность: </td><td>" + $session.должность + " </td></tr>";
    text += "</table>";
    return text;
}
//проверка Предприятие
function проверкаПредприятие(value){
    var $session = $jsapi.context().session;
    return true;
}
//Отправить предложение
function ОтправитьПредложениеЭлектроннаяПочта(){
    var $session = $jsapi.context().session;
    var text = "";
    text +="<table>"
    text += "<tr> Предложение  </tr>";
    text += "<tr><td>Ф.И.О: </td><td>" + $session.ФИО + " </td></tr>";
    text += "<tr><td>Предложение: </td><td>" + $session.предложение + " </td></tr>";
    text +="</table>" 
    return text;
}

function Расчетных_листов(){
    var $session = $jsapi.context().session;
    var list1 = ["«Корпорация Красный октябрь»",
        "АО «Корпорация Красный октябрь»",
        "АО «ТМК Нефтегазсервис-Нижневартовск»",
        "АО «ТМК НГС-Нижневартовск»",
        "ООО СинараТрансАвто",
        "ООО «СинараТрансАвто»",
        "ООО «ТМК Трубный сервис»",
        "ООО «ТМК-ЯМЗ»"];
    var list2 = [ "ООО «Предприятие «Трубопласт»","ООО «СинараПромТранс»","ПАО «Трубная металлургическая компания»",
        "ПАО «ТМК»","АО «Северский трубный завод»","АО «СТЗ»","АО «Волжский трубный завод»",
        "АО «ВТЗ»","АО «Орский машиностроительный завод»","АО «ОМЗ»",
        "АО «Синарский трубный завод»","АО «СинТЗ»","АО «Таганрогский металлургический завод»",
        "АО «ТАГМЕТ»","АО «Первоуральский Новотрубный завод»","АО «ПНТЗ»",
        "АО «Челябинский Трубопрокатный завод»","АО «ЧТПЗ»","АО «Жилевская металлобаза»",
        "«ТД «ТМК»","АО «ТД «ТМК»",
        "АО «ТМК-КПВ»","АО «ДИАЙПИ»","АО «Ракитянский арматурный завод»","АО «РАЗ»",
        "АО «РусНИТИ»","АО «Синарская ТЭЦ»","АО «Соединительные Отводы Трубопроводов»","АО «СОТ»",
        "АО «ТМК Энергосетевая компания»","АО «ТМК ЭСК»","АО «Уралчермет»","АО «ЧЗМК»",
        "АО «Челябинский завод металлоконструкций»","ООО «ТМК-ИНОКС»",
        "ООО «ТМК Трубопроводные решения»","ООО «ТМК ТР»","ООО «ЧТПЗ - Сервис»","ООО «ЧТПЗ - Сервис»",
        "ООО «ИЦ ТМК»","ООО «ИССЛЕДОВАТЕЛЬСКИЙ ЦЕНТР ТМК»",
        "ООО «РНК»","ООО «Русская Нержавеющая Компания»","ООО «РНК-Сервис»",
        "ООО «ТМК Нефтегазсервис-Бузулук»","ООО «ТМК НГС-Бузулук»",
        "ООО «ТМК НГС»","ООО «ТМК Нефтегазсервис»","ООО «ТМК НТЦ»","ООО «Научно-технический центр ТМК»",
        "ООО «ТМК Стальные Технологии»",
        "ООО «ТМК ТехСервис»","ООО «ТМК Технический Сервис»","ООО «ТМК ЭТЕРНО»","ООО «ТМК-Премиум Сервис»",
        "ООО «ТМК Центр Бизнес-услуг»","ООО «ТМК ЦБУ»",
        "БО «Сосновый бор»","Частное учреждение «База отдыха «Сосновый бор» ОАО «ПНТЗ»",
        "Частное учреждение АО «ЧТПЗ» «Дворец культуры»","Дворец культуры АО «ЧТПЗ»",
        "АО «Экорус-Первоуральск»"
        ];
    if($session.startData){
        if($session.startData.company){
            for (var i = 0, l = list1.length; i < l; i++){
                if(list1[i] == $session.startData.company){
                    return 1;
                }
            }
            for (var i = 0, l = list2.length; i < l; i++){
                if(list2[i] == $session.startData.company){
                    return 2;
                }
            } 
            
        }
    }   
    
    return 3;
    
}

function hello(){ 
    var $session = $jsapi.context().session;
    var time = Date.now();
    var текущаяДата = new Date(time);
    
    var h = (текущаяДата.getHours()+3) % 24 || 0
    
    if ((6 < h ) && (h < 12)) { return "Доброе утро";}
    if ((11 < h ) && (h < 16)) { return "Добрый день";}
    if ((15 < h ) && (h < 21)) { return "Добрый вечер";}
    return " Здравствуйте";
}