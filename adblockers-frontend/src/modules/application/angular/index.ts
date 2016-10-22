import "angular";

import {AdblockersApplicationComponent} from "./components/adblockersApplication/AdblockersApplicationComponent";
angular.module("app.application", [])
    .component("twitterApplication", new AdblockersApplicationComponent());
