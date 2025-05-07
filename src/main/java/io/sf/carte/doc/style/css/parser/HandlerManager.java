/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.uparser.TokenProducer;

abstract class HandlerManager {

	private ControlTokenHandler controlHandler;

	private HandlerManager parentManager = null;

	/**
	 * Instantiate a new manager with a new control handler.
	 * 
	 */
	public HandlerManager() {
		super();
		this.controlHandler = createControlTokenHandler();
	}

	/**
	 * Construct a manager with a parent.
	 * 
	 * @param parent the parent manager.
	 */
	protected HandlerManager(HandlerManager parent) {
		super();
		this.parentManager = parent;
		this.controlHandler = parent.getControlHandler();
	}

	abstract protected ControlTokenHandler createControlTokenHandler();

	public ControlTokenHandler getControlHandler() {
		return controlHandler;
	}

	protected HandlerManager getParentManager() {
		return parentManager;
	}

	public boolean isTopManager() {
		return getParentManager() == null;
	}

	/**
	 * Create a token producer configured for the initial stage of parsing.
	 * 
	 * @return the parser.
	 */
	public TokenProducer createTokenProducer() {
		throw new IllegalStateException("Must subclass the manager.");
	}

	/**
	 * Yield to the initial handler.
	 */
	public void restoreInitialHandler() {
		CSSTokenHandler ini = getInitialTokenHandler();
		controlHandler.yieldHandling(ini);
	}

	/**
	 * Call the {@link CSSHandler#parseStart(ParserControl)} event of the handler.
	 */
	abstract public void parseStart();

	public void rightCurlyBracket(int index) {
		controlHandler.getCurrentHandler().unexpectedCharError(index,
				TokenProducer.CHAR_RIGHT_CURLY_BRACKET);
	}

	public void yieldManagement(HandlerManager manager) {
		manager.restoreInitialHandler();

		assert checkYieldManagement(manager);
	}

	private boolean checkYieldManagement(HandlerManager manager) {
		// Let's check whether we are yielding to an ancestor
		HandlerManager mgr = parentManager;
		while (mgr != null) {
			if (mgr == manager) {
				// Yielding to parent / ancestor
				// parentManager = null;
				return true;
			}
			mgr = mgr.parentManager;
		}

		if (manager.parentManager == null) {
			// Yielding to a child
			manager.parentManager = this;
		} else if (manager.parentManager != this && manager.parentManager != parentManager) {
			throw new IllegalStateException("Possible hierarchy inconsistency");
		}
		return true;
	}

	public void restoreManagement(HandlerManager manager) {
		manager.restoreInitialHandler();

		assert checkRestoreManagement(manager);
	}

	private boolean checkRestoreManagement(HandlerManager manager) {
		// Let's check whether we are yielding to an ancestor
		HandlerManager mgr = parentManager;
		while (mgr != null) {
			if (mgr == manager) {
				// Yielding to parent / ancestor
				// parentManager = null;
				return true;
			}
			mgr = mgr.parentManager;
		}

		throw new IllegalStateException("Possible hierarchy inconsistency");
	}

	/**
	 * Call it when ending management.
	 */
	public void endManagement(int index) {
		HandlerManager p = getParentManager();
		if (p != null) {
			yieldManagement(p);
		}
	}

	public void endOfStream(int len) {
		HandlerManager p = getParentManager();
		if (p != null) {
			p.endOfStream(len);
		}
	}

	/**
	 * Get an instance of the handler that has the responsibility for the first
	 * stage of content handling.
	 * 
	 * @return the initial handler.
	 */
	abstract protected CSSTokenHandler getInitialTokenHandler();

	abstract protected CSSErrorHandler getErrorHandler();

}
