import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'sort' })
export class ArraySortPipe implements PipeTransform {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    transform(array: any, field: string): any[] {
        if (!Array.isArray(array)) {
            return;
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        array.sort((a: any, b: any) => {
            if (this.deepValue(a, field) < this.deepValue(b, field)) {
                return 1;
            } else if (this.deepValue(a, field) > this.deepValue(b, field)) {
                return -1;
            } else {
                return 0;
            }
        });
        return array;
    }

    deepValue(obj, path) {
        const pathList = path.split('.');
        for (let i = 0, len = pathList.length; i < len; i += 1) {
            obj = obj[pathList[i]];
        }
        return obj;
    }
}
