import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { FraudoEditorComponent } from './fraudo-editor.component';

describe('FraudoEditorComponent', () => {
    // The highlight is drawn under a transparent textarea; any extra whitespace in the
    // highlighted copy (for example from template formatting inside <pre>) shifts it off the text.
    it('renders the highlight with exactly the source text', () => {
        const fixture = TestBed.configureTestingModule({
            imports: [FraudoEditorComponent, NoopAnimationsModule],
        }).createComponent(FraudoEditorComponent);
        const source = 'rule: risky: in(countryBy("bin"), "RUS") -> decline;\nrule: amount() > 10 -> 3ds;';

        fixture.componentInstance.writeValue(source);
        fixture.detectChanges();

        const highlight = (fixture.nativeElement as HTMLElement).querySelector('pre.highlight');
        expect(highlight.textContent).toBe(source + '\n');
    });
});
