package ordenacao;

public class MergeSort {
    public static void ordenar(int[] arr) {
        if (arr.length < 2) return;
        mergeSort(arr, 0, arr.length - 1);
    }

    private static void mergeSort(int[] arr, int esq, int dir) {
        if (esq < dir) {
            int meio = esq + (dir - esq) / 2;
            mergeSort(arr, esq, meio);
            mergeSort(arr, meio + 1, dir);
            merge(arr, esq, meio, dir);
        }
    }

    private static void merge(int[] arr, int esq, int meio, int dir) {
        int[] temp = new int[dir - esq + 1];
        int i = esq, j = meio + 1, k = 0;

        while (i <= meio && j <= dir) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }

        while (i <= meio) temp[k++] = arr[i++];
        while (j <= dir) temp[k++] = arr[j++];

        System.arraycopy(temp, 0, arr, esq, temp.length);
    }
}