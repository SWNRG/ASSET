# Installation Update Sept 2023
All JavaFX libraries were embedded in the folder /libraries.
It was found that the project was not really portable when expecting those libraries to be found in the Java JRE/JDK.Specificallly, it only worked with specific Java flavous (Oracle), when those included all the JavaFX libraries, otherwise the project was failing on runtime (compile was ok!).
You can read more in such articles: https://stackoverflow.com/questions/51478675/error-javafx-runtime-components-are-missing-and-are-required-to-run-this-appli

With the above "hack" I tried the project in a clean Ubuntu 20 installation (September 2023). If any problems, email georgevio@gmail.com

