#!/bin/bash

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 2 \; \
	send-keys "./run_server.sh Flights" C-m \; \
	select-pane -t 3 \; \
	send-keys "./run_server.sh Cars" C-m \; \
	select-pane -t 1 \; \
	send-keys "./run_server.sh Rooms" C-m \; \
	select-pane -t 0 \; \
	send-keys "sleep 1s; ./run_middleware.sh localhost localhost localhost" C-m \;
