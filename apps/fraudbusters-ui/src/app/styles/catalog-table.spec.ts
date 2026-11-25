describe('Catalog table spacing', () => {
    let panel: HTMLElement;

    beforeEach(() => {
        panel = document.createElement('section');
        panel.className = 'fb-catalog-panel';
        panel.innerHTML = `
            <header class="table-toolbar">Records</header>
            <table class="fb-catalog-table">
                <thead><tr><th scope="col">ID</th><th scope="col">Actions</th></tr></thead>
                <tbody>
                    <tr>
                        <th scope="row">Example</th>
                        <td class="fb-catalog-action-cell">
                            <button class="mat-mdc-icon-button" type="button" aria-label="More actions"></button>
                        </td>
                    </tr>
                    <tr class="details"><td colspan="2">Expanded details</td></tr>
                </tbody>
            </table>
            <footer class="table-footer">Showing 1 record</footer>
        `;
        document.body.appendChild(panel);
    });

    afterEach(() => {
        panel.remove();
    });

    it('uses the same row, controls and panel spacing for a single-line record', () => {
        const header = panel.querySelector('thead tr');
        const row = panel.querySelector('tbody tr');
        const [firstCell, lastCell] = Array.from(row.children);
        const action = lastCell.querySelector('button');
        const toolbar = panel.querySelector('.table-toolbar');
        const footer = panel.querySelector('.table-footer');

        expect(header.getBoundingClientRect().height).toBe(37);
        expect(row.getBoundingClientRect().height).toBe(56);
        expect(action.getBoundingClientRect().height).toBe(32);
        expect(getComputedStyle(firstCell).padding).toBe('8px 12px 8px 20px');
        expect(getComputedStyle(lastCell).padding).toBe('8px 20px 8px 12px');
        expect(getComputedStyle(toolbar).padding).toBe('14px 20px');
        expect(footer.getBoundingClientRect().height).toBe(44);
        expect(getComputedStyle(footer).padding).toBe('4px 20px');
        expect(panel.querySelector('.details').getBoundingClientRect().height).toBeLessThan(56);
    });
});
