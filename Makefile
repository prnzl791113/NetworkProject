JAVAC = javac
JAVA = java

SRC_DIR = src
OUT_DIR = out

CLIENT_SRC = $(SRC_DIR)/TCPClient.java
SERVER_SRC = $(SRC_DIR)/TCPServer.java
CLIENT_CLASSES = $(patsubst $(SRC_DIR)/%.java,$(OUT_DIR)/%.class,$(CLIENT_SRC))
SERVER_CLASSES = $(patsubst $(SRC_DIR)/%.java,$(OUT_DIR)/%.class,$(SERVER_SRC))

all: client server
client: $(CLIENT_CLASSES)
server: $(SERVER_CLASSES)

$(OUT_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(OUT_DIR)
	$(JAVAC) -d $(OUT_DIR) $<

clean:
	rm -rf $(OUT_DIR)
