Ссылка на скачивание собранного APK и скриншоты приложения:
https://www.dropbox.com/sh/gghev7swiyrj6ts/AAAjaacuxPoph740sb5WQeZta?dl=0  или 
https://4pda.to/forum/index.php?showtopic=1028182

Приложение использует апи тиньков банка, центрального банка России и Московской биржи для просмотра курса валют этих компаний.
На основании данных Московской биржи строится прогноз на дальнейшее изменение курса валют. 
Прогноз строится на основании трех разных методов расчтета:
1) Метод средней скользящей
2) Метод экспоненциального сглаживания
3) Метод наименьших квадратов

Также приложение позволяет мониторить с заданным интервалом курс валют Тиньков банка и уведомлять пользователя, если курс превысит/станет ниже указанного значения.
Курс валют Тиньков банка  и Центрального банка можно отобразить на виджете, на рабочем столе.
Встроенный калькулятор подскажет сколько будет стоить купить или продать заданную сумму валюты.

Краткое описание классов приложения:
/adapters/
CBmainAdapter, TCSmainAdapter - классы адаптеров для recycleview фрагмента NowFragment 

/cbjsonapi/ - набор классов для запроса данных о валютах с помощью Retrofit с сайта Центрального Банка (на текущий и прошедший дни)
для фрагмента NowFragment с ответом в формате Json 

/cbxmlapi/ - набор классов для запроса данных о валютах с помощью Retrofit с сайта Центрального Банка (на указанный пользователем период)
для ArchiveFragment с ответом в формате Xml

/moexapi/ - набор классов для запроса данных о валютах с помощью Retrofit с сайта Московской биржи (на указанный пользователем период)
для фрагментов TodayFragment и ArhiveFragment  с ответом в формате Json

/tcsapi/ - набор классов для запроса данных о валютах с помощью Retrofit с сайта Тиньков (на текущий момент) для фрагмента NowFragment с ответом в формате Json

/database/ - набор классов для работы с Sql базой данных с помощью Room (запись в базу данных результатов обновления валют виджета приложения)

/forecastsMethods/ 
ExponentSmooth - рассчет прогноза методом экспоненциального сглаживания
LessSquare - рассчет прогноза методом наименьших квадратов
WMA - рассчет прогноза методом средней скользящей

/prefs/ 
ArchivePreferences, TodayPreferences - классы для сохранения и чтения пользовательского выбора для фрагментов ArhiveFragment  и TodayFragment

/ui/ - набор package, отвечающих за UI, состоит из:
/archive/
ArchiveFragment - фрагмент, отвечающий за экран Архив
ArchiveViewModel - реализация viewmodel для соответствующего фрагмента
DatePickerFragment - фрагмент выбора дат для экрана Архив
/calc/
CalcFragment - фрагмент, отвечающий за экран Калькулятор
CalcModel - реализация viewmodel для соответствующего фрагмента
/now/
NowFragment - фрагмент, отвечающий за экран Сейчас
NowModel - реализация viewmodel для соответствующего фрагмента
/today/
TodayFragment - фрагмент, отвечающий за экран Сегодня
TodayModel - реализация viewmodel для соответствующего фрагмента

/updateWorkers/ - workers для обновления информации о курсах валют для соответствующих фрагментов

CurrenciesApplication - Application класс для инициализации CurrenciesRepository для работы с базой данных

DateConverter - вспомогательный класс с функциями форматирования даты в нужные форматы для формирования запроса к серверам
Saver - вспомогательный класс для сохранения/загрузки данных о курсах Тиньков банка, получаемых сервисом TCSUpdateService

MainActivity - главная активити

SettingsActivity - активити настроек автообновления

TCSUpdateService - сервис, запускаемый для автообновления курсов Тиньков банка

Bootreceiver - broadcast receiver для получения информации, после перезагрузки, было ли у пользователя активировано автообновление курсов
запускающий сервис TCSUpdateService, если оно было активировано


 The application uses the api Tinkov Bank, the Central Bank of Russia and the Moscow Exchange to view the exchange rates of these companies.
Based on the data of the Moscow Exchange, a forecast is made for further changes in the exchange rate.
The forecast is based on three different calculation methods:
1) Moving average method
2) Exponential smoothing method
3) Least squares method
The application also allows you to monitor the exchange rate of Tinkov Bank at a specified interval and notify the user if the rate exceeds / falls below the specified value.
The exchange rates of Tinkov Bank and the Central Bank can be displayed on the widget, on the desktop.
The built-in calculator will tell you how much it will cost to buy or sell a given amount of currency.

A short description of the application classes:
/ adapters /
CBmainAdapter, TCSmainAdapter - adapter classes for recycleview NowFragment

/ cbjsonapi / - a set of classes for requesting data about currencies using Retrofit from the Central Bank website (for the current and past days)
for the NowFragment fragment with a response in Json format

/ cbxmlapi / - a set of classes for requesting data on currencies using Retrofit from the Central Bank website (for a user-specified period)
for ArchiveFragment with a response in Xml format

/ moexapi / - a set of classes for requesting currency data using Retrofit from the Moscow Exchange website (for a user-specified period)
for TodayFragment and ArhiveFragment fragments with a response in Json format

/ tcsapi / - a set of classes for requesting data about currencies using Retrofit from the Tinkov website (at the moment)
for a NowFragment fragment with a response in Json format

/ database / - a set of classes for working with Sql database using Room (recording the results of updating the currencies of the application widget into the database)

/ forecastsMethods /
ExponentSmooth - calculation of the forecast by the method of exponential smoothing
LessSquare - Least Squares Forecast
WMA - calculating the forecast by the moving average method

/ prefs /
ArchivePreferences, TodayPreferences - classes for saving and reading custom selection for ArhiveFragment and TodayFragment fragments

/ ui / the set of packages responsible for the UI, consists of:
/ archive /
ArchiveFragment - a fragment responsible for the Archive screen
ArchiveViewModel - the viewmodel implementation for the corresponding fragment
DatePickerFragment - Date picker fragment for Archive screen
/ calc /
CalcFragment - a fragment responsible for the Calculator screen
CalcModel - the viewmodel implementation for the corresponding fragment
/ now /
NowFragment - a fragment responsible for the Now screen
NowModel - the viewmodel implementation for the corresponding fragment
/ today /
TodayFragment - a fragment responsible for the Today screen
TodayModel - the viewmodel implementation for the corresponding fragment

/ updateWorkers / - workers to update the exchange rate information for the relevant snippets

CurrenciesApplication - Application class for initializing CurrenciesRepository for working with the database

DateConverter - a helper class with functions for formatting dates in the required formats to form a request to servers
Saver - an auxiliary class for saving / loading data on Tinkov bank rates received by the TCSUpdateService service

MainActivity - main activity

SettingsActivity - activates auto update settings

TCSUpdateService - a service launched for auto-update of Tinkov Bank rates

Bootreceiver - broadcast receiver to receive information, after reboot, whether the user had enabled autoupdate courses. If it was enabled- TCSUpdateService starts  

    
 
  

