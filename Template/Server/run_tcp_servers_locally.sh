#!/bin/bash 

#TODO: SPECIFY THE HOSTNAMES OF 4 CS MACHINES (lab1-1, cs-2, etc...)
MACHINES=("localhost" "localhost" "localhost")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 1 \; \
	send-keys "./run_tcp_server.sh Flights 6111" C-m \; \
	select-pane -t 2 \; \
	send-keys "./run_tcp_server.sh Cars 6222" C-m \; \
	select-pane -t 3 \; \
	send-keys "./run_tcp_server.sh Rooms 6333" C-m \; \
	select-pane -t 0 \; \
	send-keys " sleep 1s; ./run_tcp_middleware.sh localhost 6111 localhost 6222 localhost 6333" C-m \;
