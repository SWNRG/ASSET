# ASSET: An Sdn-inspired intruSion dEtection sysTem for Rpl
A centralized (SDN-paradigm) controller to monitor IoT networks. In this implementation, it cooperates with Cooja, the contiki emulator to monitor RPL networks.
**The project was the basis for a publication in Elsevier Future Generation Computer Systems**, https://doi.org/10.1016/j.future.2021.07.013

<img src='/pics/2-attacks.png' width=800/>

This is a controller for IoT Networks running RPL protocol. It follows the SDN paradigm, hence it only communicates with the sink.
The controller is able to identify an intruder, and the attacked nodes.
[![Intro video](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=W8hz-U6VLXo&feature=youtu.be)
The controller will automaticailly discover the underlying network, monitor in real time, an depict changes. A video example [![here](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=ElScUBguE1o), where the network, starts and then node no 7 changes position. After a while (remember, RPL takes time to adjust), the node's new position and parent are automatically depicted in the GUI.
An actual attack is identified in this [![video](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=YX4NEkfIO64&t=50s), by two attacker (purple color). The network nodes are connected via the attackers who are implementing two attacks: rank attack & grayhole attack.
After minute ~5, the attack is identified. the controller runs kMeans algoritm, finds all suspicious nodes, runs a Kosaraju algorithm to find how many stronlgy connected graphs there are, and at the end finds the mother of each such sub-graph. The "mother(s)" identified are the attackers.
As a meta-step, the network can exclude the attackers from being selected as parents by using "coloring" from a previous work, a video [![here](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=AeK3yW6pnWY&t=19s), papers [here](https://ieeexplore.ieee.org/abstract/document/8832178) & [here](https://ieeexplore.ieee.org/abstract/document/8647237).

You can freely run, modify, adapt it, use it for your research project. You also need a contiki OS with specificly adapted nodes (sink-client-intruder) in order to run experiments.
The "proffesional" way which gives you access to all, is to download contiki TWICE:
1. the contiki (slightly altereed with a lot of custom messages, etc.) from [![here](https://github.com/SWNRG/contiki-standard-extra-functions)]. Go to contiki/examples/ipv6/rpl-udp-fresh/ and run one of the many \*.csc files there. they all use the same two nodes: udp-server.c and udp-client.c. The \*dixon\*.csc emulations are using the respective \*dixon*.c sink/client code. 
2. In order to include one or more intruder node(s), you have to download another contriki version (completely separated), from ![contiki-malicious](https://github.com/SWNRG/contiki-malicious), or ![contiki-malicious-controller-aware](https://github.com/SWNRG/contiki-malicious-controller-aware), or ![contiki-malicious-controller-aware-version-attack](https://github.com/SWNRG/contiki-malicious-controller-aware-version-attack). Again, in all cases, the intruder code is in contiki/examples/ipv6/rpl-udp-fresh/\*.c.

## 2024 UPDATE: Compiler details
**Compiler plays a pivotal role**. I used gcc 5.4.0 20160609. Sceenshot below:
<img src='/pics/compiler.png' width=300/>

## 2023 INSTALLATION UPDATE
All JavaFX libraries were embedded in the folder /libraries.
It was found that the project was not really portable when expecting those libraries to be found in the Java JRE/JDK.Specificallly, it only worked with specific Java flavous (Oracle), when those included all the JavaFX libraries, otherwise the project was failing on runtime (compile was ok!).
You can read more in such articles: https://stackoverflow.com/questions/51478675/error-javafx-runtime-components-are-missing-and-are-required-to-run-this-appli
With the above "hack" I tried the project in a clean Ubuntu 20 installation (September 2023). If any problems, email georgevio@gmail.com

## HOW TO RUN IT
<img src='/pics/2022 ASSET in action.png' width=800/>
Look in the picture above for details.
Open in cooja any of the network setups inside the folders in the altered Contiki folders described above, or just create a custom network with one server node, and multiple clients. You may omit the attacking nodes. DON’T START COOJA YET.
Open ASSET project in any IDE (I run it in Eclipse, but any other one should work fine , e.g., netbeans).
Run the main.java file. It should start automatically and open two windows: 1. The GUI with the dynamic map of the network, and 2. The log serial output. In this screen, just press “Start” BEFORE you start cooja. After you start ASSET, pay attention to the log output. Is the serial port found the same with the one in cooja? If not, play with the values of “searchFromNum” and searchUpToNum” variables in the SerialProbePort.java file. Sometimes, some Ubuntu versions falsly advertise other ports as open.
If you did everything correctly, you should the message “SUCCESS, Serial Port found. Dev/pts/XX, where XX should be the same number with the one depicted in cooja in the “Serial 2 PTY” window for Serial Device (2nd line).
After a while, the logs of the discovered nodes will start appearing, and the network GUI should be filled accordingly.
As soon as you implement attackers into the network, you may start experimenting with the other buttons (kMeans, Print Edges, Chebyshev).

## DONT FORGER TO CITE...
George Violettas, George Simoglou, Sophia Petridou, Lefteris Mamatas,
A Softwarized Intrusion Detection System for the RPL-based Internet of Things networks,
Future Generation Computer Systems,
Volume 125, 2021,
Pages 698-714,
ISSN 0167-739X,
https://doi.org/10.1016/j.future.2021.07.013.

## SPECIAL THANKS
Kyriakos Vougioukas (vougioukaskyriakos@live.com) created a superb testing framework in Python, fully parameterizable, which was used for extensive tests of Dixon-Q test, and Chebyshev's Inequality. He also made it freely avaliable, [!here](https://github.com/boygioykaskyriakos/outliers_platform). You can obviously use it for other purposes. I sincerely thank him...

## SCREENSHOTS

### Controller in Action 1
<img src='/pics/controller-in-action.png' width=700/>

### Controller in Action 2
<img src='/pics/multi-small.png' width=700/>

### Controller in Action 3
<img src='/pics/attacker-loures-5-clients.png' width=700/>

### Controller in Action 4
<img src='/pics/random-screenshot.png' width=700/>

### Controller in Action 5
<img src='/pics/mailicious_level_2.png' width=700/>

### Controller in Action 6
<img src='/pics/blackhole.png' width=700/>
