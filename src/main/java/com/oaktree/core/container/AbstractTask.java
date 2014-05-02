package com.oaktree.core.container;

public abstract class AbstractTask extends AbstractComponent implements IDynamicComponent {

	protected IComponentManager manager;
	private boolean paused = false;

	@Override
	public void setComponentManager(IComponentManager manager) {
		this.manager = manager;
	}

        @Override
        public IComponentManager getComponentManager() {
            return this.manager;
        }

	@Override
	public boolean isPaused() {
		return this.paused;
	}

	@Override
	public void pause() {
		this.paused = true;
	}

	@Override
	public void resume() {
		this.paused = false;
	}

	@Override
	public void onComponentStateChange(IComponent component,
			ComponentState oldState, ComponentState newState,
			String changeReason) {
	}
}
