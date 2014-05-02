package com.oaktree.core.container;

import com.oaktree.core.utils.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author IJLAPTOP
 */
public class EchoComponentListener implements IComponentManagerListener {

    private final static Logger logger = LoggerFactory.getLogger(EchoComponentListener.class);

    @Override
    public void onComponentAdded(IComponent c) {
        logger.info("Component " + c.getName() + " has been added as " + c.getComponentType().name() + Text.COLON + c.getComponentSubType());
    }

    @Override
    public void onComponentRemoved(IComponent c) {
        logger.info("Component " + c.getName() + " has been removed as " + c.getComponentType().name() + Text.COLON + c.getComponentSubType());
    }

}
