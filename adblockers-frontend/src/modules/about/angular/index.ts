import "angular";
import "angular-route";
import {config as routesConfig} from "./configs/routes";
import {PageAboutComponent} from "./components/pageAbout/PageAboutComponent";
import {PaperPdfComponent} from "./components/paperPdf/PaperPdfComponent";

angular.module("app.about", ["ngRoute"])
    .component("pageAbout", new PageAboutComponent())
    .component("paperPdf", new PaperPdfComponent())
    .config(routesConfig);
