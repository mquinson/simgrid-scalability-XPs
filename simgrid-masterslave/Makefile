all: masterslave_mailbox masterslave_mailbox_opt

INSTALL_PATH = $(shell sed 's/\#.*//' test_scalability.sh | grep 'export LD_LIBRARY' | sed 's/export LD_LIBRARY_PATH=//' | sed 's|lib||')
CC = gcc
PEDANTIC_PARANOID_FREAK =       -O0 -Wall -Wshadow -Wcast-align \
                                -Waggregate-return -Wmissing-prototypes -Wmissing-declarations \
                                -Wstrict-prototypes -Wmissing-prototypes -Wmissing-declarations \
                                -Wredundant-decls -Wnested-externs \
                                -Wpointer-arith -Wwrite-strings -finline-functions
REASONABLY_CAREFUL_DUDE =       -O2 -Wall
NO_PRAYER_FOR_THE_WICKED =      -O5 -w
WARNINGS =                      $(PEDANTIC_PARANOID_FREAK)

CFLAGS = -g $(WARNINGS)

INCLUDES = 
DEFS = -I$(INSTALL_PATH)/include -L$(INSTALL_PATH)/lib/
LDADD = -lm -lsimgrid 
LIBS = 

C_FILES = $(wildcard *.c)
BIN_FILES = $(patsubst %.c,%,$(C_FILES))
O_FILES = $(patsubst %.c,%.o,$(C_FILES))

%: %.o
	$(CC) $(INCLUDES) $(DEFS) $(CFLAGS) $^ $(LIBS) $(LDADD) -o $@ 

%.o: %.c
	$(CC) $(INCLUDES) $(DEFS) $(CFLAGS) -c -o $@ $<

clean:
	rm -f $(BIN_FILES) *.o *~
.SUFFIXES:
.PHONY : clean

