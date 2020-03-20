# coral-jfx-GUI-span
A centralized (SDN-paradigm) controller to monitor IoT networks. In this implementation, it cooperates with Cooja, the contiki emulator to monitor RPL networks.
![COntroller in Action](/20200317 mutli-attackes-id'd correctly.png)

This is a controller for IoT Networks. It follows the SDN paradigm, hence it only communicates with the sink.
The controller is able to identify an intruder, and the attacked nodes.
[![Intro video](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=W8hz-U6VLXo&feature=youtu.be)
THe controller will automaticailly discover the disovery of the underlying network, monitor in real time, an depict changes. An example [![here](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=ElScUBguE1o), where the network, starts and then node no 7 changes position. After a while (remember, RPL takes time to adjust), the node's new position and parent are automatically depicted in the GUI.
An actual attack is identified [![here](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=YX4NEkfIO64&t=50s), by two attacker (purple color). The network nodes are connected via the attackers who are implementing two attacks: rank attack & grayhole attack.
After minute ~5, the attack is identified. the controller runs kMeans algoritm, finds all suspicious nodes, runs a Kosaraju algorithm to find how many stronlgy connected graphs there are, and at the end find the mother of each such sub-graph. The mothers identified are the attackers.
As a meta-step, the network can exclude the attackers from being selected as parents by using "coloring" from a previous work, a video [![here](https://img.youtube.com/vi/VID/0.jpg)](https://www.youtube.com/watch?v=AeK3yW6pnWY&t=19s), papers [here](https://ieeexplore.ieee.org/abstract/document/8832178) & [here](https://ieeexplore.ieee.org/abstract/document/8647237).



DONT FORGER TO CITE...
