/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import java.util.Objects;

import javax.swing.JToggleButton;

import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.net.SimulationServer;
import amodeus.amodeus.util.net.ObjectClient;
import amodeus.amodeus.util.net.ObjectHandler;

/* package */ class AmodeusToggleButton extends JToggleButton {

    private ObjectClient client = null;

    public AmodeusToggleButton(AmodeusComponent amodeusComponent) {
        super("connect...");

        addActionListener(event -> {
            if (Objects.nonNull(client)) {
                client.close();
                client = null;
            }
            if (isSelected()) {
                try {
                    client = new ObjectClient("localhost", SimulationServer.OBJECT_SERVER_PORT, new ObjectHandler() {
                        @Override
                        public void handle(Object object) {
                            amodeusComponent.setSimulationObject((SimulationObject) object);
                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }
}
