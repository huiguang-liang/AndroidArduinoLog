AndroidArduinoLog
=================


This App will read the Arduino log from serial port with root permission

Idea: cat /dev/ttyACM0 and use another thread to display log on device.

Notice: phone must be rooted, if not, we don't have permission to read /dev/ttyACM0

How to run: 

	Input file name on edit text (default filename is andruinolog.txt), log file will be saved in sdcard 

	Press start. 

	Press stop to stop save log to file (arduino still send data to serial port, we just ignore it)

