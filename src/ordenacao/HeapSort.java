package ordenacao;

public class HeapSort {
    public static void ordenar(int[] arr) {
        int n = arr.length;

        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        for (int i = n - 1; i > 0; i--) {
            trocar(arr, 0, i);
            heapify(arr, i, 0);
        }
    }

    private static void heapify(int[] arr, int n, int i) {
        int maior = i;
        int esq = 2 * i + 1;
        int dir = 2 * i + 2;

        if (esq < n && arr[esq] > arr[maior]) {
            maior = esq;
        }

        if (dir < n && arr[dir] > arr[maior]) {
            maior = dir;
        }

        if (maior != i) {
            trocar(arr, i, maior);
            heapify(arr, n, maior);
        }
    }

    private static void trocar(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}