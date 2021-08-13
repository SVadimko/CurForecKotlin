<h1 align="center">Тиньков курсы валют</h1>
<p align="center">
  <a href="#"><img alt="Android Badge" src="https://badgen.net/badge/OS/Android?icon=https://raw.githubusercontent.com/androiddevnotes/learn-jetpack-compose-android/master/assets/android.svg&color=3ddc84"/></a>
</p>
<p align="center">
Ссылка на скачивание собранного APK и скриншоты приложения:
https://www.dropbox.com/sh/gghev7swiyrj6ts/AAAjaacuxPoph740sb5WQeZta?dl=0  или 
https://4pda.to/forum/index.php?showtopic=1028182
</p>
<p align="center">

![alt text](https://github.com/SVadimko/CurForecKotlin/blob/master/assets/1.jpg)
![alt text2](https://github.com/SVadimko/CurForecKotlin/blob/master/assets/2.jpg)
![alt text3](https://github.com/SVadimko/CurForecKotlin/blob/master/assets/3.jpg)
</p>
<p align="center">

![alt text4](https://github.com/SVadimko/CurForecKotlin/blob/master/assets/4.jpg)
</p>

Приложение использует апи тиньков банка, центрального банка России и Московской биржи для просмотра
курса валют этих компаний. На основании данных Московской биржи строится прогноз на дальнейшее
изменение курса валют. Прогноз строится на основании трех разных методов расчтета:

1. Метод средней скользящей
1. Метод экспоненциального сглаживания
1. Метод наименьших квадратов

Также приложение позволяет мониторить с заданным интервалом курс валют Тиньков банка и уведомлять
пользователя, если курс превысит/станет ниже указанного значения. Курс валют Тиньков банка и
Центрального банка можно отобразить на виджете, на рабочем столе. Встроенный калькулятор подскажет
сколько будет стоить купить или продать заданную сумму валюты.

# Ссылка на задокументированный код:
https://github.com/SVadimko/CurForecKotlin/tree/master/documentation/html


# Краткое описание пакетов и классов приложения:

## /adapters/
CBmainAdapter, TCSmainAdapter - классы адаптеров для recycleview фрагмента NowFragment

## /cbjsonApi/
Набор классов для запроса данных о валютах с помощью Retrofit с сайта Центрального
Банка (на текущий и прошедший дни)
для фрагмента NowFragment с ответом в формате Json

## /cbxmlApi/
Набор классов для запроса данных о валютах с помощью Retrofit с сайта Центрального
Банка (на указанный пользователем период)
для ArchiveFragment с ответом в формате Xml

## /moexApi/
Набор классов для запроса данных о валютах с помощью Retrofit с сайта Московской биржи (
на указанный пользователем период)
для фрагментов TodayFragment и ArhiveFragment с ответом в формате Json

## /tcsApi/
Набор классов для запроса данных о валютах с помощью Retrofit с сайта Тиньков (на текущий
момент) для фрагмента NowFragment с ответом в формате Json

## /database/
Набор классов для работы с Sql базой данных с помощью Room (запись в базу данных
результатов обновления валют виджета приложения)

## /forecastsMethods/
ExponentSmooth - рассчет прогноза методом экспоненциального сглаживания
LessSquare - рассчет прогноза методом наименьших квадратов WMA - рассчет прогноза методом средней
скользящей

## /utils/
Пакет вспомогательных объектов

## /ui/archive/
ArchiveFragment - фрагмент, отвечающий за экран Архив
ArchiveViewModel - реализация viewmodel для соответствующего фрагмента
DatePickerFragment - фрагмент выбора дат для экрана Архив


## /ui/calc/
CalcFragment - фрагмент, отвечающий за экран Калькулятор
CalcModel - реализация viewmodel для соответствующего фрагмента

## /ui//now/
NowFragment - фрагмент, отвечающий за экран Сейчас
NowModel - реализация viewmodel для соответствующего фрагмента

## /ui/today/
TodayFragment - фрагмент, отвечающий за экран Сегодня
TodayModel - реализация viewmodel для соответствующего фрагмента

# /updateWorkers/
Workers для обновления информации о курсах валют для соответствующих фрагментов

## /widget/
Классы, реализующие функционал виджета

# CurrenciesApplication -
Application класс (инициализация CurrenciesRepository для работы с базой
данных и запуск KOIN(DI), создание синглтона контекста приложения)

## MainActivity
Главная активити

## SettingsActivity
Активити настроек автообновления

## TCSUpdateService
Сервис, запускаемый для автообновления курсов Тиньков банка

## Bootreceiver
Broadcast receiver для получения информации, после перезагрузки, было ли у
пользователя активировано автообновление курсов запускающий сервис TCSUpdateService, если оно было
активировано

# CoinsAnimator
Вспомогательный класс для запуска анимации монет при обновлении курса

<h1 align="center">Tinkoff exchange rates</h1>

The application uses the api Tinkov Bank, the Central Bank of Russia and the Moscow Exchange to view
the exchange rates of these companies. Based on the data of the Moscow Exchange, a forecast is made
for further changes in the exchange rate. The forecast is based on three different calculation
methods:
gr
1. Moving average method
1. Exponential smoothing method
1. Least squares method The application also allows you to monitor the exchange rate of Tinkov Bank
   at a specified interval and notify the user if the rate exceeds / falls below the specified
   value. The exchange rates of Tinkov Bank and the Central Bank can be displayed on the widget, on
   the desktop. The built-in calculator will tell you how much it will cost to buy or sell a given
   amount of currency.

# Link to code documentation:
https://github.com/SVadimko/CurForecKotlin/tree/master/documentation/html

# A short description of the application classes:

## / adapters /
CBmainAdapter, TCSmainAdapter - adapter classes for recycleview NowFragment

## / cbjsonApi /
A set of classes for requesting data about currencies using Retrofit from the
Central Bank website (for the current and past days)
for the NowFragment fragment with a response in Json format

## / cbxmlApi /
A set of classes for requesting data on currencies using Retrofit from the Central
Bank website (for a user-specified period)
for ArchiveFragment with a response in Xml format

## / moexApi /
A set of classes for requesting currency data using Retrofit from the Moscow Exchange
website (for a user-specified period)
for TodayFragment and ArhiveFragment fragments with a response in Json format

## / tcsApi /
A set of classes for requesting data about currencies using Retrofit from the Tinkov
website for a NowFragment fragment with a response in Json format

## / database /
A set of classes for working with Sql database using Room (recording the results of
updating the currencies of the application widget into the database)

## / forecastsMethods /
ExponentSmooth - calculation of the forecast by the method of exponential
smoothing
LessSquare - Least Squares Forecast
WMA - calculating the forecast by the moving average
method

## / utils /
Package with helper objects

## / ui /
Set of packages responsible for the UI, consists of:

## / updateWorkers /
Workers to update the exchange rate information for the relevant snippets

## /widget/
Classes that implement the functionality of the widget

## CurrenciesApplication
Application class (initializing CurrenciesRepository for working with the
database, start KOIN(DI) and create singleton of application context)

## MainActivity
Main activity

## SettingsActivity
Activates auto update settings

## TCSUpdateService
A service launched for auto-update of Tinkov Bank rates

## Bootreceiver
Broadcast receiver to receive information, after reboot, whether the user had enabled autoupdate courses.
If it was enabled- TCSUpdateService starts

## CoinsAnimator
A helper class for triggering animation of coins when the rate is updated    
 
  

