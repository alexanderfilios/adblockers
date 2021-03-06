import IScope = angular.IScope;
import {Constants} from "../../../core/Constants";
export class AdblockersApplicationComponent implements ng.IComponentOptions{
    public controller: Function = TwitterApplicationController;
    public template: string = `
        <nav class="navbar navbar-inverse navbar-fixed-top">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a class="navbar-brand" href="#/about">Adblocker Privacy</a>
                </div>
                <div id="navbar" class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li ng-repeat="menuItem in menuItems">
                            <a href="{{menuItem.link}}"
                                ng-class="{'dropdown-toggle': isExpandable(menuItem)}"
                                ng-attr-aria-expanded="{{isExpandable(menuItem) && 'false' || ''}}"
                                ng-attr-aria-haspopup="{{isExpandable(menuItem) && 'true' || ''}}"
                                ng-attr-data-toggle="{{isExpandable(menuItem) && 'dropdown' || ''}}"
                                ng-attr-role="{{isExpandable(menuItem) && 'button' || ''}}"
                                title="{{menuItem.title}}">
                                <span class="{{'glyphicon glyphicon-' + menuItem.icon}}" style="margin-right: 10px; color: gray; vertical-align: middle;"></span>{{menuItem.text}}
                                <span ng-if="isExpandable(menuItem)" class="caret"></span>
                            </a>
                            <ul ng-if="isExpandable(menuItem)" class="dropdown-menu">
                                <li ng-repeat="subMenuItem in menuItem.subMenuItems"
                                    ng-attr-role="{{subMenuItem.separator && 'separator' || ''}}"
                                    ng-attr-class="{{subMenuItem.separator && 'divider' || ''}}">
                                    <a ng-if="subMenuItem.link" href="{{subMenuItem.link || ''}}"
                                        title="{{subMenuItem.title || ''}}">
                                        {{subMenuItem.text || submenuItem.section || ''}}
                                    </a>
                                    <span style="font-weight: bold; padding-left: 10px;" ng-if="!subMenuItem.link">{{subMenuItem.section}}</span>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
        <div style="padding-left: 20%; padding-top: 10px;" ng-view></div>`;
}

interface IMenuScope extends IScope {
    isExpandable: Function;
    menuItems: Array<any>;
}
export class TwitterApplicationController {
    public static $inject: Array<string> = ['$scope'];
    constructor(scope: IMenuScope) {
        scope.isExpandable = function(menuItem: any): boolean {
            return menuItem && Array.isArray(menuItem.subMenuItems)
                && menuItem.subMenuItems.length > 0;
        };
        scope.menuItems = [
            {
                title: 'About',
                text: 'About',
                link: '#/about',
                icon: 'th',
                subMenuItems: []
            },
            {
                title: 'Temporal evolution',
                text: 'Temporal evolution',
                link: '#',
                icon: 'signal',
                subMenuItems: [
                    {
                        section: 'Domain graphs'
                    },
                    {
                        title: 'Domain FPD mean node degree',
                        text: 'FPD mean node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE'].name
                    },
                    {
                        title: 'Domain FPD stdev node degree',
                        text: 'FPD stdev node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_STDEV'].name
                    },
                    {
                        title: 'Domain FPD mean node degree (Top 500)',
                        text: 'FPD mean node degree (Top 500)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_TOP_500'].name
                    },
                    {
                        title: 'Domain FPD mean node degree (Last 500)',
                        text: 'FPD mean node degree (Last 500)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_LAST_500'].name
                    },
                    {
                        title: 'Domain FPD mean node degree (Top 10)',
                        text: 'FPD mean node degree (Top 10)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_TOP_10'].name
                    },
                    {
                        title: 'Domain FPD mean node degree (Top 1)',
                        text: 'FPD mean node degree (Top 1)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_TOP_1'].name
                    },
                    {
                        title: 'Domain TPD mean node degree',
                        text: 'TPD mean node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE'].name
                    },
                    {
                        title: 'Domain TPD stdev node degree',
                        text: 'TPD stdev node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE_STDEV'].name
                    },
                    {
                        title: 'Domain TPD mean node degree (Top 10)',
                        text: 'TPD mean node degree (Top 10)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE_TOP_10'].name
                    },
                    {
                        title: 'Domain TPD mean node degree (Top 1)',
                        text: 'TPD mean node degree (Top 1)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE_TOP_1'].name
                    },
                    {
                        title: 'Domain density',
                        text: 'Density',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['DENSITY'].name
                    },
                    {
                        title: 'All domain graphs',
                        text: 'All graphs',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['DOMAIN_GRAPH'].name
                    },
                    {
                        separator: true
                    },
                    {
                        section: 'Entity graphs'
                    },
                    {
                        title: 'Entity FPD mean node degree',
                        text: 'FPD node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE'].name
                    },
                    {
                        title: 'Entity FPD mean node degree (Top 10)',
                        text: 'FPD node degree (Top 10)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_TOP_10'].name
                    },
                    {
                        title: 'Entity FPD mean node degree (Top 1)',
                        text: 'FPD node degree (Top 1)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['FPD_DEGREE_TOP_1'].name
                    },
                    {
                        title: 'Entity TPD mean node degree',
                        text: 'TPD node degree',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE'].name
                    },
                    {
                        title: 'Entity TPD mean node degree (Top 10)',
                        text: 'TPD node degree (Top 10)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE_TOP_10'].name
                    },
                    {
                        title: 'Entity TPD mean node degree (Top 1)',
                        text: 'TPD node degree (Top 1)',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['TPD_DEGREE_TOP_1'].name
                    },
                    {
                        title: 'Entity density',
                        text: 'Density',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name + '/metric/' + Constants.METRIC_TYPES['DENSITY'].name
                    },
                    {
                        title: 'All entity graphs',
                        text: 'All graphs',
                        link: '#/linecharts/' + Constants.GRAPH_TYPES['ENTITY_GRAPH'].name
                    }

                ]
            },
            {
                title: 'Maps',
                text: 'Maps',
                link: '#',
                icon: 'map-marker',
                subMenuItems: [
                    {
                        section: 'Legal entities'
                    },
                    {
                        title: 'Legal-entity markers',
                        text: 'Markers',
                        link: '#/maps/' + Constants.MAP_TYPES['LEGAL_ENTITIES_MARKERS'].name
                    },
                    {
                        title: 'Legal entities per country',
                        text: 'Occurrences per country',
                        link: '#/maps/' + Constants.MAP_TYPES['LEGAL_ENTITIES_REGIONS'].name
                    },
                    {
                        separator: true
                    },
                    {
                        section: 'Servers'
                    },
                    {
                        title: 'Server markers',
                        text: 'Markers',
                        link: '#/maps/' + Constants.MAP_TYPES['SERVERS_MARKERS'].name
                    },
                    {
                        title: 'Servers per country',
                        text: 'Occurrences per country',
                        link: '#/maps/' + Constants.MAP_TYPES['SERVERS_REGIONS'].name
                    }
                ]
            },
            {
                title: 'Paper',
                text: 'Paper',
                link: '#/paper',
                icon: 'list-alt',
                subMenuItems: []
            }
        ];
    }
}
