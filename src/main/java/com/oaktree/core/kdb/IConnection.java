package com.oaktree.core.kdb;

import com.oaktree.core.container.IComponent;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 12/10/11
 * Time: 07:59
 */
public interface IConnection extends IComponent {
    public String getHost();
    public int getPort();
    public boolean isConnected();


}
