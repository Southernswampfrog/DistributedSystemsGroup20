#!/bin/bash 

#TODO: SPECIFY THE HOSTNAMES OF 4 CS MACHINES (lab1-1, cs-2, etc...)
MACHINES=("gclyne@cs-1.cs.mcgill.ca" "gclyne@cs-2.cs.mcgill.ca" "gclyne@cs-3.cs.mcgill.ca" "gclyne@cs-4.cs.mcgill.ca")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 2 \; \
	send-keys "ssh -t ${MACHINES[0]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Flights\"" C-m \; \
	select-pane -t 3 \; \
	send-keys "ssh -t ${MACHINES[1]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Cars\"" C-m \; \
	select-pane -t 0 \; \
	send-keys "ssh -t ${MACHINES[2]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Rooms\"" C-m \; \
	select-pane -t 1 \; \
	send-keys "ssh -t ${MACHINES[3]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; sleep .5s; ./run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}\"" C-m \;
