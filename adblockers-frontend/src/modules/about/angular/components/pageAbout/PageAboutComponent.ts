export class PageAboutComponent implements ng.IComponentOptions {
    public template: string = `
    <div class="container-fluid">
        <h1>Quantifying Web-Adblocker Privacy</h1>
        <p>Web advertisements, an integral part of today's web browsing experience, financially support countless websites. Meaningful advertisements, however, require behavioral targeting, user tracking and profile fingerprinting that raise serious privacy concerns. To counter privacy issues and enhance usability, adblockers emerged as a popular way to filter web requests that do not serve the website's main content. Despite their popularity, little work has focused on quantifying the privacy provisions of adblockers.</p>
        <p>In this paper, we develop a quantitative approach to objectively compare the privacy of adblockers. We propose a model based on a set of privacy metrics that captures not only the technical web architecture, but also the underlying corporate institutions of the problem across time and geography.</p>
        <p>We investigate experimentally the effect of various combinations of ad-blocking software and browser settings on 1000 Web sites. Our results highlight a significant difference among adblockers in terms of filtering performance, in particular affected by the applied configurations. Besides the ability to judge the filtering capabilities of existing adblockers and their particular configurations, our work provides a general framework to evaluate new adblocker proposals.</p>
    </div>`;
}
