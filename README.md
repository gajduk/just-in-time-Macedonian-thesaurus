Just-in Time Macedonian thesaurus
========================

Whenever you need to look up the meaning of a Macedonian word this thesaurus is ready to help.
It is available directly from Microsoft Word using a Word Add-id which is a one-click-to-install application.
Planing to make it available for LibreOffice Writer and some PDF viewers.

The application provides definitions and translations aggregated from different Macedonian websites, and also does look up on Wikipedia to provide references and information on important people, places, events and more.


-----------------


The application features a two-level application. On the user end we have the Word Add In (and soon, the writer and PDF plug-ins).
The user-level invokes a web service in the application level that fetches word definitions and translations from the web.
This architecture keeps the user level real simple and enables us to focus more on the look and feel of the plug-ins.
On the other hand in the application level we can focus more on performance issues.


The web service is implemented in Java using the Play Framework. It uses the [https://code.google.com/p/concurrentlinkedhashmap/]ConcurentLinkedHashMap from Google for efficient caching on frequently queried words. Furthermore, it uses the Java built-in threading support to concurrently fetch information from multiple websites which speeds up querying even more.

----------------

Web service requirements
 - Play 1.2.7
 - ConcurentLinkedHashMap 1.4
 - Gson 2.3.1
 - java 6 or up
 
Word add-in recommended setup
 - JsonDeserialization module
 - Visual Studio 2013 with Office/SharePoint and Visual C#

----------------

Screenshots from the word add-in

![alt tag](https://raw.githubusercontent.com/gajduk/just-in-time-Macedonian-thesaurus/master/img1.PNG)

![alt tag](https://raw.githubusercontent.com/gajduk/just-in-time-Macedonian-thesaurus/master/img2.PNG)

![alt tag](https://raw.githubusercontent.com/gajduk/just-in-time-Macedonian-thesaurus/master/img3.PNG)

![alt tag](https://raw.githubusercontent.com/gajduk/just-in-time-Macedonian-thesaurus/master/img4.PNG)

![alt tag](https://raw.githubusercontent.com/gajduk/just-in-time-Macedonian-thesaurus/master/img5.PNG)
