module Plugin

import lang::missgrant::syntax::MissGrant;
import util::IDE;
import ParseTree;


public void main() {
	registerLanguage("Controller", "ctl", (str input, loc org) {
		return parse(#Controller, input, org);
		}
	);
}