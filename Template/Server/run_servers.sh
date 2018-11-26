#!/bin/bash 

MACHINES=("cs-1.cs.mcgill.ca" "cs-3.cs.mcgill.ca" "cs-5.cs.mcgill.ca" "cs-4.cs.mcgill.ca")

tmux new-session \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
	select-layout main-vertical \; \
	select-pane -t 2 \; \
	send-keys "ssh -t 'gclyne@'${MACHINES[0]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Flights ${MACHINES[3]}\"" C-m \; \
	select-pane -t 3 \; \
	send-keys "ssh -t 'gclyne@'${MACHINES[1]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Cars ${MACHINES[3]}\"" C-m \; \
	select-pane -t 1 \; \
	send-keys "ssh -t 'gclyne@'${MACHINES[2]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; ./run_server.sh Rooms ${MACHINES[3]}\"" C-m \; \
	select-pane -t 0 \; \
	send-keys "ssh -t 'gclyne@'${MACHINES[3]} \"cd Template/Server > /dev/null; echo -n 'Connected to '; hostname; sleep 1s; ./run_middleware.sh ${MACHINES[0]} ${MACHINES[1]} ${MACHINES[2]}\"" C-m \;
