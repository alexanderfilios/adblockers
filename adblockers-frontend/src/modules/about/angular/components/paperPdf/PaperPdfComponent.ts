import {Constants} from "../../../../application/core/Constants";
/**
 * Created by alexandrosfilios on 24/09/16.
 */
export class PaperPdfComponent implements ng.IComponentOptions {
    public template: string =
        '<embed src="' + Constants.BASE_URL + 'resources/paper" \
                width="500"\
                height="700"\
                type="application/pdf"></embed>';
};
