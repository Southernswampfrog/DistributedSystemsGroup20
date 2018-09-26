#!/bin/bash 

#TODO: SPECIFY THE HOSTNAMES OF 4 CS MACHINES (lab1-1, cs-2, etc...)
MACHINES=("localhost" "localhost" "localhost")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 2 \; \
	send-keys "./run_server.sh Flights" C-m \; \
	select-pane -t 3 \; \
	send-keys "./run_server.sh Cars" C-m \; \
	select-pane -t 0 \; \
	send-keys "./run_server.sh Rooms" C-m \; \
	select-pane -t 1 \; \
	send-keys " sleep .5s; ./run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}" C-m \;
