#!/bin/bash 

#TODO: SPECIFY THE HOSTNAMES OF 4 CS MACHINES (lab1-1, cs-2, etc...)
MACHINES=("localhost" "localhost" "localhost")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 2 \; \
	send-keys "./run_tcp_server.sh Flights localhost 6000" C-m \; \
	select-pane -t 3 \; \
	send-keys "./run_tcp_server.sh Cars localhost 6001" C-m \; \
	select-pane -t 0 \; \
	send-keys "./run_tcp_server.sh Rooms localhost 6002" C-m \; \
	select-pane -t 1 \; \
	send-keys " sleep .5s; ./run_tcp_middleware.sh 5999 localhost 6000 localhost 6001 localhost 6002" C-m \;
